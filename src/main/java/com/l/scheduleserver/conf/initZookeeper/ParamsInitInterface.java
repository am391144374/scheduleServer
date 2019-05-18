package com.l.scheduleserver.conf.initZookeeper;


import java.io.File;
import java.util.HashMap;

public interface ParamsInitInterface {


    /**
     * 初始化连接信息
     */
    public void initConnectionParam();

    public HashMap<String,Object> getParams(File file, String fileType);
}
