package com.l.scheduleserver.bean;


import com.l.scheduleserver.enums.container;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.*;

public class WorkerServiceInfo {

    //保存worker子节点的data信息
    public static List<String> serverInfo = new LinkedList<>();
    //该集合只被master使用,用与存储定时任务列表
    public static ConcurrentHashMap<String,ScheduleBean> workerSchedule = new ConcurrentHashMap<>();

    /**
     * 读写锁
     * 实际本程序可以不使用读写锁
     * 读写锁为后续非数据库并发添加定时任务
     */
    private static ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
    private static ReadLock readLock = reentrantReadWriteLock.readLock();
    private static WriteLock writeLock = reentrantReadWriteLock.writeLock();

    public static String getAllServerInfo(){
        StringBuffer stringBuffer = new StringBuffer();
        for(String str : serverInfo){
            stringBuffer.append(str + container.WHIPPLETREE);
        }
        if(stringBuffer.length() > 0){
            stringBuffer.substring(0,stringBuffer.length() - 1);
        }
        return stringBuffer.toString();
    }

    public static ScheduleBean getWork(String scheduleName){
        if(scheduleName != null){
            return workerSchedule.get(scheduleName);
        }
        return null;
    }

    public static void removeServer(String serverData){
        serverInfo.remove(serverData);
    }

    public static ConcurrentHashMap getWorks(){
        if(workerSchedule.size() <= 0){
            //未录入定时任务
            return null;
        }
        return workerSchedule;
    }

    public static boolean putWork(ScheduleBean scheduleBean){
        try {
            workerSchedule.put(scheduleBean.getScheduleName(), scheduleBean);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public static int getWorksSize(){
        if(workerSchedule.size() <= 0){
            return -1;
        }
        return workerSchedule.size();
    }
}
