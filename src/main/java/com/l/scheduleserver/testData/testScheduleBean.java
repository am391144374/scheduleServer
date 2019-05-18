package com.l.scheduleserver.testData;

import com.l.scheduleserver.bean.ScheduleBean;
import com.l.scheduleserver.bean.WorkerServiceInfo;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;

/**
 * 测试数据
 */
@Component
public class testScheduleBean {

    @Bean
    public void getScheduleBean(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MONTH,3);
        ScheduleBean scheduleBean = new ScheduleBean();
        scheduleBean.setCreateTime(new Date());
        scheduleBean.setCreateUserId("liushijie");
        scheduleBean.setCreateUserName("ROOT");
        scheduleBean.setCron("* 1 * * * ?");
        scheduleBean.setDesc("测试的定时任务");
        scheduleBean.setEndTime(calendar.getTime());
        scheduleBean.setRunJob(new scheduleData());
        scheduleBean.setScheduleId(3386);
        scheduleBean.setScheduleName("测试1");
        scheduleBean.setStartTime(new Date());
        scheduleBean.setVersion("1");
        WorkerServiceInfo.putWork(scheduleBean);
    }

}
