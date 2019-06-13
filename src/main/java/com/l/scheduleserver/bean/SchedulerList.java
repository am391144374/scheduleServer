package com.l.scheduleserver.bean;

import org.quartz.Scheduler;

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 保存已经添加经quartz的定时任务
 */
public class SchedulerList {

    //保存已经存在的定时任务，便于后续启停与更新
    private static ConcurrentHashMap<Integer,Scheduler> schedulerWorkList = new ConcurrentHashMap<>();

    public static boolean checkExists(Integer id){
        return schedulerWorkList.contains(id);
    }

    public static void putScheduler(Integer id,Scheduler scheduler){
        schedulerWorkList.put(id,scheduler);
    }

    public static Scheduler getScheduler(Integer id){
        if(!checkExists(id)){
            return null;
        }
        return schedulerWorkList.get(id);
    }

    public static boolean removeScheduler(Integer id,Scheduler scheduler){
        if(!checkExists(id)){
            return false;
        }
        return schedulerWorkList.remove(id,scheduler);
    }

    public static int getSize(){
        return schedulerWorkList.size();
    }

    public static List<Scheduler> getAllScheduler(){
        if(schedulerWorkList.size() > 0){
            List<Scheduler> schedulerList = new LinkedList<>();
            Enumeration<Integer> keys = schedulerWorkList.keys();
            while(keys.hasMoreElements()){
                Integer key = keys.nextElement();
                schedulerList.add(schedulerWorkList.get(key));
            }
            return schedulerList;
        }else{
            return null;
        }
    }
}
