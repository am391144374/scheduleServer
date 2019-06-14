package com.l.scheduleserver.controller;


import com.l.scheduleserver.services.quartzService.QuartzService;
import com.l.scheduleserver.bean.ScheduleBean;
import com.l.scheduleserver.bean.WorkerServiceInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
     * @param id
     */
    @GetMapping(METHODPATH)
    public void receiveMessage(@RequestParam("data")Integer id){
        log.info("新增任务---{}",id);
        ScheduleBean scheduleBean = WorkerServiceInfo.getWork(id);
        if(scheduleBean != null){
            quartzService.addJob(scheduleBean);
        }else{
            log.warn(id+",未找到对应的定时任务");
        }
    }
}
