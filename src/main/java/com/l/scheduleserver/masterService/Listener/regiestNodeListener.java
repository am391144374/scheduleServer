//package com.l.scheduleserver.masterService.Listener;
//
//import org.apache.curator.framework.CuratorFramework;
//import org.apache.curator.framework.recipes.cache.PathChildrenCache;
//
//import java.util.concurrent.ThreadFactory;
//
///**
// * 注册类
// */
//public class regiestNodeListener {
//
//    /**
//     * 监听
//     */
//    private PathChildrenCache pathChildrenCache;
//
//    public regiestNodeListener(CuratorFramework curatorFramework, String listerenPath, Boolean cacheData, ThreadFactory threadFactory){
//
//        regiestListener();
//    }
//
//    public void start() throws Exception {
//        pathChildrenCache.start();
//    }
//
//    public void regiestListener(){
//        pathChildrenCache.getListenable().addListener(new PathCacheListener());
//    }
//}
