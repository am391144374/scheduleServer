package com.l.scheduleserver.exampleScheduleBean;

import com.l.scheduleserver.conf.DefaultJobAndFail;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;

@Slf4j
public class scheduleData extends DefaultJobAndFail {

    @Override
    public void execute(JobExecutionContext jobExecutionContext){
        JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();
//        DefaultFail defaultFail = (DefaultFail) jobDataMap.get("fail");
//        defaultFail.fail();
        fail();
        log.info("{},定时任务开始执行",jobDataMap.get("desc"));
        JobKey jobKey = jobExecutionContext.getTrigger().getJobKey();
        log.info("执行{},完成！",jobKey.getName());
    }
}
