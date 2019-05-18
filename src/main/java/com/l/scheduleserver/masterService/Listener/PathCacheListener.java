package com.l.scheduleserver.masterService.Listener;

import com.l.scheduleserver.bean.WorkerServiceInfo;
import com.l.scheduleserver.enums.container;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;

import java.util.concurrent.TimeUnit;

import static org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type.*;

@Slf4j
public class PathCacheListener implements PathChildrenCacheListener {

    private InterProcessMutex interProcessMutex;

    public PathCacheListener(CuratorFramework curatorFramework,String lockPath){
        this.interProcessMutex = new InterProcessMutex(curatorFramework,lockPath);
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
                    WorkerServiceInfo.removeServer(serverData);
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
