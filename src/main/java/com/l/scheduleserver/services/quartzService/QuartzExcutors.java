package com.l.scheduleserver.services.quartzService;

import com.l.scheduleserver.bean.ScheduleBean;
import com.l.scheduleserver.bean.SchedulerList;
import com.l.scheduleserver.enums.container;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.quartz.TriggerBuilder.newTrigger;

/**
 * 执行job任务类
 */
@Slf4j
@Component
public class QuartzExcutors {

    /**
     * 启动任务
     * @throws SchedulerException
     */
    public void start(Scheduler scheduler){
        try {
            if(!scheduler.isStarted()){
                scheduler.start();
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭任务
     * @throws SchedulerException
     */
    public static void stop(Scheduler scheduler){
        try {
            if(!scheduler.isShutdown()){
                scheduler.shutdown();
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    /**
     * 新增定时任务
     * @param scheduleBean
     * @throws SchedulerException
     */
    public void addJobFromId(ScheduleBean scheduleBean) throws RuntimeException, SchedulerException {
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        String jobKeyString = scheduleBean.getScheduleId()+ container.WHIPPLETREE+scheduleBean.getScheduleName();
        JobKey jobKey = new JobKey(jobKeyString);
        TriggerKey triggerKey = new TriggerKey(jobKeyString);
        if(!scheduler.checkExists(jobKey)){
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("desc",scheduleBean.getDesc());
            jobDataMap.put("fail",scheduleBean.getFailFactory());
            JobDetail jobDetail = JobBuilder.newJob(scheduleBean.getRunJob().getClass())
                    .setJobData(jobDataMap)
                    .withIdentity(jobKey)
                    .storeDurably()
                    .build();
            scheduler.addJob(jobDetail,true);

            Trigger cronTrigger = newTrigger().startAt(scheduleBean.getStartTime()).endAt(scheduleBean.getEndTime())
                    .withIdentity(triggerKey)
                    .withSchedule(CronScheduleBuilder.cronSchedule(scheduleBean.getCron()))
                    .forJob(jobDetail).build();
            //校验是否有已经存在的定时，如果与原来的不同则刷新
            if(scheduler.checkExists(triggerKey)){
                Trigger oldCronTrigger = scheduler.getTrigger(triggerKey);
                if(!oldCronTrigger.equals(cronTrigger)){
                    scheduler.rescheduleJob(triggerKey,cronTrigger);
                }
            }
            scheduler.scheduleJob(cronTrigger);
            SchedulerList.putScheduler(scheduleBean.getScheduleId(),scheduler);
            start(scheduler);
        }else{
            throw new RuntimeException(jobKeyString+"已经存在无法再添加！");
        }
        log.info("add job success! jobName={},jobCron={},startTime={},endTime={}",jobKeyString
                ,scheduleBean.getCron(),scheduleBean.getCreateTime(),scheduleBean.getEndTime());
    }

    /**
     * 删除job任务
     */
    public boolean deleteJobFromId(Integer scheduleId){
        Scheduler scheduler;
        if((scheduler =SchedulerList.getScheduler(scheduleId)) != null){
            stop(scheduler);
            SchedulerList.removeScheduler(scheduleId,scheduler);
            return true;
        }else{
            log.info("{}--未找到对应的定时任务",scheduleId);
            return false;
        }
    }

    //更新定时任务，未实现调用
    public void updateJobFromId(ScheduleBean scheduleBean)throws RuntimeException{
        //判断job是否存在
        int scheduleId = scheduleBean.getScheduleId();
        if(SchedulerList.checkExists(scheduleId)){
            deleteJobFromId(scheduleId);
            try {
                addJobFromId(scheduleBean);
            } catch (SchedulerException e) {
                log.error("{}--添加失败！,失败原因：",scheduleId,e);
            }
        }else{
            throw new RuntimeException(scheduleId+"--没有这个任务");
        }
    }

}