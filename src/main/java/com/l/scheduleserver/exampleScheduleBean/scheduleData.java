package com.l.scheduleserver.exampleScheduleBean;

import com.l.scheduleserver.conf.DefaultJobAndFail;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;

@Slf4j
public class scheduleData extends DefaultJobAndFail {

    @Override
    public void execute(JobExecutionContext jobExecutionContext){
        try {
            JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();
            log.info("{},定时任务开始执行",jobDataMap.get("desc"));
            JobKey jobKey = jobExecutionContext.getTrigger().getJobKey();
            log.info("执行{},完成！",jobKey.getName());
            fail();
        }catch (Exception e){
            e.printStackTrace();
            fail();
        }

    }

    @Override
    public void fail() {
        log.error("新定义的错误");
    }

}
