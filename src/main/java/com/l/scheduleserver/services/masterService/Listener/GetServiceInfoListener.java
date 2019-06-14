package com.l.scheduleserver.services.masterService.Listener;

import com.l.scheduleserver.bean.WorkerServiceInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.util.List;

import static com.l.scheduleserver.enums.container.*;

@Slf4j
public class GetServiceInfoListener implements Runnable{

    private CuratorFramework curatorFramework;

    public GetServiceInfoListener(CuratorFramework curatorFramework){
        this.curatorFramework = curatorFramework;
    }


    @Override
    public void run() {
        if(WorkerServiceInfo.serverInfo.size() > 0){
            log.info("当前的所有worker信息,serviceInfo={}",WorkerServiceInfo.getAllServerInfo());
        }
        WorkerServiceInfo.serverInfo.clear();

        try {
            List<String> serviceInfos =  curatorFramework.getChildren().forPath(ZK_WORKER_PATH);
            for (String ob : serviceInfos){
                byte[] data = curatorFramework.getData().forPath(ZK_WORKER_PATH +SPRITE + ob);
                WorkerServiceInfo.serverInfo.add(new String(data));
            }
            log.info("重新获取后，当前的所有worker信息,serviceInfo={}",WorkerServiceInfo.getAllServerInfo());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
