package com.l.scheduleserver.services.masterService.Listener;

import com.l.scheduleserver.bean.ScheduleBean;
import com.l.scheduleserver.bean.WorkerServiceInfo;
import com.l.scheduleserver.enums.container;
import com.l.scheduleserver.services.dao.ScheduleDao;
import com.l.scheduleserver.util.httpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.l.scheduleserver.enums.container.*;
import static org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type.*;

@Slf4j
public class PathCacheListener implements PathChildrenCacheListener {

    private ScheduleDao scheduleDao;
    private String lockPath;

    public PathCacheListener(String lockPath, ScheduleDao scheduleDao){
        this.lockPath = lockPath;
        this.scheduleDao = scheduleDao;
    }

    /**
     * 监听到子节点变更的处理逻辑
     * @param curatorFramework
     * @param pathChildrenCacheEvent
     * @throws Exception
     */
    @Override
    public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent){
        if(!isMaster){
            log.info("非master不做监听处理");
            return;
        }
        InterProcessMutex interProcessMutex = new InterProcessMutex(curatorFramework,lockPath);
        PathChildrenCacheEvent.Type cacheType = pathChildrenCacheEvent.getType();
        ChildData childData = pathChildrenCacheEvent.getData();
        if(cacheType.equals(CHILD_ADDED)){
            log.info("新节点新增节点：{}，数据：{}",childData.getPath(),childData.getData());
            WorkerServiceInfo.serverInfo.add(new String(childData.getData()));
        }else if(cacheType.equals(CHILD_REMOVED)){
            try {
                interProcessMutex.acquire(container.PROCESS_MUTEX_TIME,TimeUnit.SECONDS);
                if(!interProcessMutex.isAcquiredInThisProcess()){
                    log.error("当前服务无法持有锁,不做操作");
                    return;
                }
                if(!interProcessMutex.isOwnedByCurrentThread()){
                    return;
                }else{
                    //更新节点信息
                    String serverData = childData.getPath();
                    String data = new String(childData.getData());
                    String appName = data.split(WHIPPLETREE)[0];
                    log.info("删除节点信息------{}",serverData);
                    if(!WorkerServiceInfo.removeServer(data)){
                        log.error("删除节点失败-------{}",serverData);
                        return;
                    }
                    /**
                     * 1.从redis中获取故障的服务已经派发的所有任务
                     * 2.根据ID获取相应的定时任务
                     * 3.将故障服务的定时任务分配到目前还存在的worker中
                     */
                    long paramSize = scheduleDao.searchSizeByAppName(appName);
                    if(paramSize == 0){
                        log.info("{}未配置相关任务",appName);
                        return;
                    }
                    if(paramSize > 0){
                        List<String> childDatas = WorkerServiceInfo.serverInfo;
                        while(childDatas.size() <= 0){
                            log.info("没有在活动的主机等待5秒再判断");
                            childDatas = WorkerServiceInfo.serverInfo;
                            Thread.sleep(5000);
                        }
                        long splitNum = (paramSize / childDatas.size()) + (paramSize % childDatas.size());
                        String server = ((LinkedList<String>) childDatas).poll();
                        for(int num = 0 ; num < paramSize ; num++){
                            if(num % splitNum == 0 && num > 0){
                                server = ((LinkedList<String>) childDatas).poll();
                            }
                            Object schId = null;
                            if((schId = scheduleDao.rpopByAppName(appName)) == null){
                                break;
                            }
                            ScheduleBean scheduleBean = WorkerServiceInfo.getWork((Integer) schId);
                            //截取出IP地址和应用名，通过调用拼接IP地址来派发定时任务。
                            String address = server.split(WHIPPLETREE)[1];
                            String newAppName = server.split(WHIPPLETREE)[0];
                            //通过http调用的方式来分发数据
                            httpUtil.sendSchedul(address,METHODPATH,scheduleBean);
                            scheduleDao.insertScheduleByAppName(newAppName,scheduleBean.getScheduleId());
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
                log.error("处理删除节点错误：{}",e.getMessage());
            }finally {
                //释放分布式锁
                try {
                    interProcessMutex.release();
                } catch (Exception e) {
                    log.error("释放锁失败：{}",e.getMessage());
                }
            }
        }
    }
}
