package com.l.scheduleserver.ScheduleService;

import com.l.scheduleserver.bean.ScheduleBean;
import com.l.scheduleserver.quartz.QuartzExcutors;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
/**
 * 添加job类
 */
public class QuartzService {

    @Autowired
    private QuartzExcutors quartzExcutors;

    public boolean addJob(ScheduleBean scheduleBean){
        try {
            quartzExcutors.addJobFromId(scheduleBean);
            log.info("添加任务成功，job={}"+scheduleBean.toString());
            return true;
        } catch (SchedulerException e) {
            e.printStackTrace();
            log.error("添加失败");
            return false;
        } catch (RuntimeException e){
            log.error("已经存在相同的JOB，请先删除再添加");
            return false;
        }
    }

    public boolean deleteJob(String scheduleId,String scheduleName){
        try {
            quartzExcutors.deleteJobFromId(Integer.valueOf(scheduleId));
            log.info("删除成功,name={}",scheduleName);
            return true;
        } catch (RuntimeException e){
            log.error("删除的服务不存在，name={}",scheduleName);
            return false;
        }
    }

}
