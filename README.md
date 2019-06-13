# 分布式定时任务程序

#### 介绍
基于zookeeper实现的分布式定时任务程序。

#### 软件架构
包含：

1.zookeeper

2.quartz

3.curator

4.httpclient

5.springboot

####描述
1.是同curator内置的选举类来操作选举master。
2.master不会处理定时任务，所以至少部署两台服务。
3.master在成为leader时会传输定时名字来分配任务。

####使用
1.配置zookeeper的连接地址（目前写死在AbstractInitParam类中）。
2.编写定时任务，需要使用注解@Schedule标注类，@ScheduleGetBeanFromMethod标注Bean方法，具体请查看com.l.scheduleserver.exampleScheduleBean包下的例子。

#### 使用说明
当前程序只完成了分布的分配与注册定时任务的获取，定时任务只能写在Java类中继承Job接口

剩余需要完成项：

1.zookeeper的连接配置后续支持：xml格式解析、properties格式解析

2.引入netty（或者将分发改为rabbitmq的订阅模式，worker自己获取定时任务）

3.增加告警提醒：短信提醒、邮件提醒

4.保存已经分配的定时任务，防止在后续master服务宕机或者异常，新的master不需要再次分配定时任务