package com.l.scheduleserver.masterService.Listener;


import com.l.scheduleserver.bean.ScheduleBean;
import com.l.scheduleserver.bean.WorkerServiceInfo;
import com.l.scheduleserver.util.ThreadUtils;
import com.l.scheduleserver.util.httpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.zookeeper.CreateMode;

import java.io.Closeable;
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
//    private final LeaderSelector leaderSelector;
    private final CuratorFramework curatorFramework;
    private String hostName;

    public SelectLeader(String name, String path, CuratorFramework curatorFramework,String hostName){
        this.name = name;
        this.hostName = hostName;
        //选举master
        this.curatorFramework = curatorFramework;
        setHeartBeatToGetInfo();
    }

    /**
     * 注册获取worker信息
     */
    private void setHeartBeatToGetInfo(){
        ScheduledExecutorService scheduledExecutorService = ThreadUtils.getScheduledExecutorService(GET_SERVERINFO_HEARTBEAT_NAME,GET_SERVERINFO_POOLSIZE);
        scheduledExecutorService.scheduleAtFixedRate(new GetServiceInfoListener(curatorFramework),2,GET_SERVERINFO_TIME, TimeUnit.SECONDS);
        log.info("注册获取信息监听程序成功");
    }

    @Override
    public void isLeader() {
        log.info("当前{}成为leader",name);
        try{
            String MasterPath = ZK_MASTER_PATH + "/" + name;
            String WorkerPath = ZK_WORKER_PATH + "/" + name;
            String data = name + WHIPPLETREE + hostName;
            if(curatorFramework.checkExists().forPath(MasterPath) == null){
                //先从worker删除，再创建一个临时的master节点
                curatorFramework.delete().forPath(WorkerPath);
                curatorFramework.create().creatingParentContainersIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(MasterPath,data.getBytes());
            }
            log.info("初始化分发定时任务开始！");
            //进行定时任务的分派
//            List<String> childPath =  curatorFramework.getChildren().forPath(ZK_WORKER_PATH);
            List<String> childDatas = WorkerServiceInfo.serverInfo;
            while(childDatas.size() < 1){
                log.info("master没有获取到worker，等待10秒后再次获取！");
                log.info("当前worker数量：{}",childDatas.size());
                Thread.sleep(10000);
                childDatas = WorkerServiceInfo.serverInfo;
            }

            /**
             * 理论上定时任务应该是从数据库中查询出来，则在获取master的时候就会初始化所有的定时任务
             */
            int workSize = WorkerServiceInfo.getWorksSize();
            ConcurrentHashMap<String,ScheduleBean> works = WorkerServiceInfo.getWorks();
            while(workSize == -1 || works == null){
                //每10秒查询一次
                log.info("未查找到worker子节点！10秒后再次获取！");
                log.info("当前worker任务的数量：{}",workSize);
                Thread.sleep(10000);
                works =  WorkerServiceInfo.getWorks();
                workSize = WorkerServiceInfo.getWorksSize();
            };
            //根据worker的数量和当前定时任务量来均衡分配
            int splitNum = (workSize / childDatas.size()) + (workSize % childDatas.size());
            String server = ((LinkedList<String>) childDatas).poll();
            Object[] key = works.keySet().toArray();
            for(int i = 0 ; i < key.length ; i++){
                ScheduleBean scheduleBean = works.get(key[i]);
                if(i == splitNum){
                    server = ((LinkedList<String>) childDatas).poll();
                }
                //截取出IP地址和应用名，通过调用拼接IP地址来派发定时任务。
                String address = server.split(WHIPPLETREE)[1];
                //解析出appName的原因是后续可以对每个worker处理的定时任务做保存方便master易主后分配定时任务
                String appName = server.split(WHIPPLETREE)[0];
                //通过http调用的方式来分发数据
                httpUtil.sendSchedul(address,METHODPATH,scheduleBean);
            }
            log.info("初始化分发定时任务完成！");
            //初始化完成后一直保持master不退出
            while(true){
                Thread.sleep(Integer.MAX_VALUE);
            }
        }catch ( InterruptedException e ){
            log.error(name + " was interrupted.");
            Thread.currentThread().interrupt();
        }catch (Exception e){
            e.printStackTrace();
        }finally{
            log.info("竞选完成-放弃leader");
        }
    }

    @Override
    public void notLeader() {
        log.info("当前{}未成为master",name);
    }
}
