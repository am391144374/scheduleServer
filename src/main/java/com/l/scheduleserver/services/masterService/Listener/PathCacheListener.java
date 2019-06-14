package com.l.scheduleserver.services.masterService.Listener;

import com.l.scheduleserver.bean.WorkerServiceInfo;
import com.l.scheduleserver.conf.DefaultFail;
import com.l.scheduleserver.conf.DefaultJobAndFail;
import com.l.scheduleserver.enums.container;
import com.l.scheduleserver.services.dao.ScheduleDao;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.l.scheduleserver.enums.container.*;
import static org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type.*;

@Slf4j
public class PathCacheListener extends DefaultFail implements PathChildrenCacheListener {

    private InterProcessMutex interProcessMutex;
    private ScheduleDao scheduleDao;

    public PathCacheListener(CuratorFramework curatorFramework, String lockPath, ScheduleDao scheduleDao){
        this.interProcessMutex = new InterProcessMutex(curatorFramework,lockPath);
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
        PathChildrenCacheEvent.Type cacheType = pathChildrenCacheEvent.getType();
        ChildData childData = pathChildrenCacheEvent.getData();
        if(cacheType.equals(CHILD_ADDED)){
            log.info("新节点新增节点：{}，数据：{}",childData.getPath(),childData.getData());
            WorkerServiceInfo.serverInfo.add(new String(childData.getData()));
        }else if(cacheType.equals(CHILD_REMOVED)){
            try {
                if(interProcessMutex.acquire(container.PROCESS_MUTEX_TIME,TimeUnit.SECONDS)){
                    //更新节点信息
                    String serverData = childData.getPath();
                    String data = String.valueOf(childData.getData());
                    String appName = data.split(WHIPPLETREE)[0];
                    WorkerServiceInfo.removeServer(serverData);
                    /**
                     * 1.获取故障的worker的所有定时任务的ID
                     * 2.更具ID获取相应的定时任务
                     * 3.将故障服务的定时任务分配到目前还存在的worker中
                     */

                    List<Object> params = scheduleDao.searchAllScheduleIdByAppName(appName);
                    scheduleDao.deleteAllByAppName(appName);

                }else{
                    //其他进程在做相同的事情。
                    log.error("当前主机无法获取锁，不做其他处理！");
                }
            }catch (Exception e){
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