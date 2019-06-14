package com.l.scheduleserver.bean;

import com.l.scheduleserver.conf.FailFactory;
import com.sun.istack.internal.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.quartz.Job;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@ToString
public class ScheduleBean implements Serializable {

    //定时任务的唯一ID
    @NotNull
    private int scheduleId;
    //定时任务的名字
    private String scheduleName;
    //轮询时间
    private String cron;
    //创建时间
    private Date createTime;
    //更新时间
    private Date updateTime;
    //开始时间
    private Date startTime;
    //结束时间
    private Date endTime;
    //定时任务描述内容
    private String desc;
    //创建定时任务人
    private String createUserName;
    //创建定时任务人ID
    private String createUserId;
    //需要执行的业务逻辑，使用该字段需解决传输类
    private Job runJob;
    //当任务执行失败时，执行该任务
    private FailFactory failFactory;
    //当前定时任务的版本，用于更新定时任务，只会更新版本比当前任务版本更改的任务
    private String version;
}
