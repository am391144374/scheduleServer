package com.l.scheduleserver.ScheduleService;

/**
 * 当定时任务执行失败时运行该任务
 */
public interface FailFactory {

    public void fail(String scheduleName);
}
