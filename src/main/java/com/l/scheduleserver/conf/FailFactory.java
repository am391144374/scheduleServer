package com.l.scheduleserver.conf;

/**
 * 当定时任务执行失败时运行该任务
 */
public interface FailFactory {

    public void fail();
}
