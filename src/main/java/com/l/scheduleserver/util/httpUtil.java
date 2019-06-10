package com.l.scheduleserver.util;

import com.alibaba.fastjson.JSONObject;
import com.l.scheduleserver.bean.ScheduleBean;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;

@Slf4j
public class httpUtil {

    /**
     * 发送定时任务
     * @param address
     * @param scheduleBean
     * @param methodPath
     */
    public static void sendSchedul(String address,String methodPath, ScheduleBean scheduleBean){
        HttpClient client = HttpClients.createDefault();
        //传输ScheduleName
        HttpGet httpGet = new HttpGet("http://"+address + methodPath + "?data="+scheduleBean.getScheduleName());
        try {
            HttpResponse httpResponse = client.execute(httpGet);
            if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                log.info("{}--传输成功",scheduleBean.getScheduleName());
            }
        } catch (IOException e) {
            log.error("传输失败，失败原因：",e);
        }
    }

}
