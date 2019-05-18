package com.l.scheduleserver.conf.initZookeeper;

import org.apache.curator.RetryPolicy;
import org.apache.curator.retry.RetryUntilElapsed;
import org.springframework.stereotype.Component;

public abstract class AbstractInitParam implements ParamsInitInterface {

    public String connectString = "192.168.184.129:2181";
    public int sessionTimeOut = 60;
    public int connectionTimeOut = 60;
    public RetryPolicy retryPolicy = new RetryUntilElapsed(1000,3);

    public void initConnectionParam() {
        //预留初始化文件解析方法，目前不做处理
//        HashMap<String,Object> hashMap = getParams(new File(""),"file");
//        this.connectString = (String)hashMap.get("connectionString");
//        this.sessionTimeOut = (Integer)hashMap.get("sessionTimeOut");
//        this.connectionTimeOut = (Integer)hashMap.get("connectionTimeOut");
//        this.retryPolicy = (RetryPolicy)hashMap.get("retryPolicy");
    }

}
