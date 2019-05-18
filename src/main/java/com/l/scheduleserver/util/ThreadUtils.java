package com.l.scheduleserver.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

public class ThreadUtils {

    /**
     * 获取
     * @param threadName
     * @param poolSize
     * @return
     */
    public static ScheduledExecutorService getScheduledExecutorService(String threadName,int poolSize){
        ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(poolSize,setThreadFactoryByName(threadName));
        return scheduledExecutorService;
    }

    /**
     * 设置监听线程名
     * @param threadName
     * @return
     */
    public static ThreadFactory setThreadFactoryByName(String threadName){
        ThreadFactory threadFactory = new ThreadFactoryBuilder().
                setNameFormat(threadName)
                .setDaemon(true)
                .build();
        return threadFactory;
    }
}
