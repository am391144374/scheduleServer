package com.l.scheduleserver.controller;


import com.l.scheduleserver.ScheduleService.QuartzService;
import com.l.scheduleserver.bean.ScheduleBean;
import com.l.scheduleserver.bean.WorkerServiceInfo;
import com.l.scheduleserver.quartz.QuartzExcutors;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.l.scheduleserver.enums.container.*;

/**
 * 此类为接收master的添加定时任务请求
 */
@RestController
@Slf4j
public class ReceiveJob {

    @Autowired
    private QuartzService quartzService;

    /**
     * 获取到master发送的job后，先保存不做处理，等待触发任务做处理
     * @param data
     */
    @GetMapping(METHODPATH)
    public void receiveMessage(@RequestParam("data")String data){
        log.info("新增任务---{}",data);
        ScheduleBean scheduleBean = WorkerServiceInfo.getWork(data);
        if(scheduleBean != null){
            quartzService.addJob(scheduleBean);
        }else{
            log.warn(data+",未找到对应的定时任务");
        }
    }

}
