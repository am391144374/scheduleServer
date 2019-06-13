package com.l.scheduleserver.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class serverUtil {

    @Value("${server.port}")
    private String port;
    @Value("${spring.application.name}")
    private String appName;
    @Value("${ip.address}")
    private String address;

    public String getHost(){
        return address + ":" + port;
    }
    public String getApplicationName(){
        return appName;
    }
    public String getUUID(){
        return String.valueOf(UUID.randomUUID());
    }
}
