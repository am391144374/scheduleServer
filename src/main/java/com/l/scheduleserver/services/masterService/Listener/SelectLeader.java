package com.l.scheduleserver.services.masterService.Listener;


import com.l.scheduleserver.bean.ScheduleBean;
import com.l.scheduleserver.bean.SchedulerList;
import com.l.scheduleserver.bean.WorkerServiceInfo;
import com.l.scheduleserver.enums.container;
import com.l.scheduleserver.services.dao.ScheduleDao;
import com.l.scheduleserver.services.quartzService.QuartzExcutors;
import com.l.scheduleserver.util.ThreadUtils;
import com.l.scheduleserver.util.httpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.quartz.Scheduler;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.l.scheduleserver.enums.container.*;

/**
 * zookeeper 选举策略
 * 初始化发送定时任务给worker
 */
@Slf4j
public class SelectLeader implements LeaderLatchListener {

    private final String name;
    private final CuratorFramework curatorFramework;
    private final String masterPath;
    private ScheduleDao scheduleDao;
    private int workerNum;
    private ScheduledExecutorService scheduledExecutorService = null;

    public SelectLeader(String name, String path, CuratorFramework curatorFramework,ScheduleDao scheduleDao,int workerNum){
        this.name = name;
        this.masterPath = path;
        this.scheduleDao = scheduleDao;
        this.workerNum = workerNum;
        //选举master
        this.curatorFramework = curatorFramework;
    }

    /**
     * 注册获取worker信息
     */
    private void setHeartBeatToGetInfo(){
        scheduledExecutorService = ThreadUtils.getScheduledExecutorService(GET_SERVERINFO_HEARTBEAT_NAME,GET_SERVERINFO_POOLSIZE);
        scheduledExecutorService.scheduleAtFixedRate(new GetServiceInfoListener(curatorFramework),2,GET_SERVERINFO_TIME, TimeUnit.SECONDS);
        log.info("注册获取信息监听程序成功");
    }

    @Override
    public void isLeader() {
        log.info("当前{}成为leader",name);
        isMaster = true;
        //当成为leader时才会启动监听
        setHeartBeatToGetInfo();
        try{
            String MasterPath = masterPath + "/" + name;
            String WorkerPath = ZK_WORKER_PATH + "/" + name;
//            String data = name + WHIPPLETREE + hostName;
            //如果从worker晋升为leader则删除定时任务和清空worker保存的worker数量，等待重新分配
            if(curatorFramework.checkExists().forPath(WorkerPath) != null){
                isLeaderThenDeleteWorkerInfo();
                //从worker删除
                curatorFramework.delete().forPath(WorkerPath);
            }
            log.info("初始化分发定时任务开始！");
            List<String> childDatas = WorkerServiceInfo.serverInfo;
            while(childDatas.size() < workerNum){
                log.info("master没有获取到worker，等待5秒后再次获取！");
                log.info("当前worker数量：{}",childDatas.size());
                Thread.sleep(5000);
                childDatas = WorkerServiceInfo.serverInfo;
            }

            /**
             * 理论上定时任务应该是从数据库中查询出来，则在获取master的时候就会初始化所有的定时任务
             */
            int workSize = WorkerServiceInfo.getWorksSize();
            ConcurrentHashMap<String,ScheduleBean> works = WorkerServiceInfo.getWorks();
            while(workSize == -1 || works == null){
                //每10秒查询一次
                log.info("未查找到worker子节点！5秒后再次获取！");
                log.info("当前worker任务的数量：{}",workSize);
                Thread.sleep(5000);
                works =  WorkerServiceInfo.getWorks();
                workSize = WorkerServiceInfo.getWorksSize();
            };
            //根据worker的数量和当前定时任务量来均衡分配
            int splitNum = (workSize / childDatas.size()) + (workSize % childDatas.size());
            String server = ((LinkedList<String>) childDatas).poll();
            Object[] key = works.keySet().toArray();
            for(int i = 0 ; i < key.length ; i++){
                ScheduleBean scheduleBean = works.get(key[i]);
                if(i % splitNum == 0 && i > 0){
                    server = ((LinkedList<String>) childDatas).poll();
                }
                //截取出IP地址和应用名，通过调用拼接IP地址来派发定时任务。
                String address = server.split(WHIPPLETREE)[1];
                //解析出appName的原因是后续可以对每个worker处理的定时任务做保存方便master易主后分配定时任务
                String appName = server.split(WHIPPLETREE)[0];
                //通过http调用的方式来分发数据
                httpUtil.sendSchedul(address,METHODPATH,scheduleBean);
                scheduleDao.insertScheduleByAppName(appName,scheduleBean.getScheduleId());

            }
            log.info("初始化分发定时任务完成！");
        }catch ( InterruptedException e ){
            log.error(name + " was interrupted.");
            Thread.currentThread().interrupt();
            shutdownListeren();
            isMaster = false;
        }catch (Exception e){
            shutdownListeren();
            isMaster = false;
            e.printStackTrace();
        }finally{
            log.info("竞选完成-放弃leader");
        }
    }

    @Override
    public void notLeader() {
        log.info("当前{}未成为master",name);
        isMaster = false;
    }

    public void isLeaderThenDeleteWorkerInfo(){
        if(SchedulerList.getSize() > 0){
            List<Scheduler> schedulerList = SchedulerList.getAllScheduler();
            for(Scheduler scheduler : schedulerList){
                QuartzExcutors.stop(scheduler);
            }
        }
        WorkerServiceInfo.serverInfo.clear();
    }

    //出现错误或者其他原因放弃leader则关闭监听
    private void shutdownListeren(){
        if(scheduledExecutorService != null){
            scheduledExecutorService.shutdownNow();
        }
    }
}
