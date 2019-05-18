package com.l.scheduleserver.controller;


import com.alibaba.fastjson.JSONObject;
import com.l.scheduleserver.bean.ScheduleBean;
import com.l.scheduleserver.bean.WorkerServiceInfo;
import com.l.scheduleserver.quartz.QuartzExcutors;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
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

    /**
     * 获取到master发送的job后，先保存不做处理，等待触发任务做处理
     * @param data
     */
    @GetMapping(METHODPATH)
    public void receiveMessage(@RequestParam("data")String data){
        ScheduleBean scheduleBean = JSONObject.parseObject(data,ScheduleBean.class);
        log.info("新增任务---{}",scheduleBean.toString());
        WorkerServiceInfo.putWork(scheduleBean);
        try {
            QuartzExcutors.getInstance().addJobFromId(scheduleBean);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

}
