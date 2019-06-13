package com.l.scheduleserver.conf.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Schedule {
    //当前定时任务是否启动
    boolean start() default true;
}
