package com.l.scheduleserver.ScheduleService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultFail implements FailFactory {


    /**
     * 默认打印日志
     * @param scheduleName 定时任务的名字
     */
    public void fail(String scheduleName) {
        log.error("定时任务{}执行失败",scheduleName);
    }
}
