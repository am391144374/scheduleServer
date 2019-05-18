package com.l.scheduleserver.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class serverUtil {

//    private static InetAddress instanceos;


    private static String port = "1235";

    private static String appName = "liu1";

    private static String address = "localhost";

    @Value("${port}")
    public void setPort(String port){
        this.port = port;
    }
    @Value("${appname}")
    public void setAppName(String appName){
        this.appName = appName;
    }
    @Value("${IP.ADDRESS}")
    public void setAddress(String address){
        this.address = address;
    }

//    static {
//        try {
//            instanceos = InetAddress.getLocalHost();
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        }
//    }

    public static String getHost(){
        return address + ":" + port;
    }

    public static String getApplicationName(){
//        return instanceos.getHostName();
       return appName;
    }
    public static String getUUID(){
        return String.valueOf(UUID.randomUUID());
    }

}
