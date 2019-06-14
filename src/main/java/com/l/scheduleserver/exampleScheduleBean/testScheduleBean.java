package com.l.scheduleserver.exampleScheduleBean;

import com.l.scheduleserver.conf.DefaultFail;
import com.l.scheduleserver.bean.ScheduleBean;
import com.l.scheduleserver.conf.annotation.Schedule;
import com.l.scheduleserver.conf.annotation.ScheduleGetBeanFromMethod;

import java.util.Calendar;
import java.util.Date;

/**
 * 测试数据
 */
@Schedule
public class testScheduleBean {

    /**
     *
     * @return 需要返回的是一个ScheduleBean的方法
     */
    @ScheduleGetBeanFromMethod
    public ScheduleBean getScheduleBean(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MONTH,3);
        ScheduleBean scheduleBean = new ScheduleBean();
        scheduleBean.setCreateTime(new Date());
        scheduleBean.setCreateUserId("liushijie");
        scheduleBean.setCreateUserName("ROOT");
        scheduleBean.setCron("*/1 * * * * ?");
        scheduleBean.setDesc("测试的定时任务");
        scheduleBean.setEndTime(calendar.getTime());
        scheduleBean.setRunJob(new scheduleData());
        scheduleBean.setScheduleId(3386);
        scheduleBean.setScheduleName("测试1");
        scheduleBean.setStartTime(new Date());
        scheduleBean.setVersion("1");
        scheduleBean.setFailFactory(new DefaultFail());
        return scheduleBean;
    }

}
