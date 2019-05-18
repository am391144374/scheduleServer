package com.l.scheduleserver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class ServerApplication {

    /**
     * 当前程序只完成了分布的分配与注册定时任务的获取，定时任务只能写在Java类中继承Job接口
     * 剩余需要完成项：
     * 1.定义多种可执行的定时任务：shell脚本、数据库操作、python脚本执行（不确定是否可行）
     * 2.zookeeper的连接配置后续支持：xml格式解析、properties格式解析
     * 3.将HTTPCLIENT调用修改为rpc调用，引入netty（或者将分发改为rabbitmq的订阅模式，worker自己获取定时任务）
     * 4.增加告警提醒：短信提醒、邮件提醒
     * 5.保存已经分配的定时任务，防止在后续master服务down机或者异常，新的master不需要再次分配定时任务
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
        log.info("服务启动完成！");
    }

}
