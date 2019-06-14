package com.l.scheduleserver.conf;

import lombok.extern.slf4j.Slf4j;

@Slf4j
//后续做为告警邮件、短信入口
public class DefaultFail implements FailFactory {

    /**
     * 默认打印日志
     */
    public void fail() {
        log.error("定时任务执行失败");
    }
}
