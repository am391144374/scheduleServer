package com.l.scheduleserver.services.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Slf4j
public class ScheduleDao {

    @Autowired
    protected RedisTemplate redisTemplate;

    /**
     * 此方法会保存各个服务所分配的定时任务ID
     * @param appName
     * @param value 定时任务ID
     */
    @Transactional
    public void insertScheduleByAppName(String appName,int value){
        if(!checkIsNotExist(appName,value)){
            redisTemplate.opsForList().rightPush(appName,value);
        }
    }

    private boolean checkIsNotExist(String appName,int value){
        List<Object> Ids =  searchAllScheduleIdByAppName(appName);
        if(Ids != null){
            return Ids.contains(value);
        }else{
            return false;
        }
    }

    /**
     * 获得全部的值
     * @param appName
     * @return
     */
    public List<Object> searchAllScheduleIdByAppName(String appName){
        long size = searchSizeByAppName(appName);
        if(size == 0){
            return null;
        }
        return redisTemplate.opsForList().range(appName,0,size);
    }

    /**
     * 获取传入的worker所分配的大小
     * @param appName
     * @return
     */
    private Long searchSizeByAppName(String appName){
        return redisTemplate.opsForList().size(appName);
    }

    /**
     * 查询在list中最后的一个值
     * @param appName
     * @return
     */
    private Object serchLastScheduleIdByAppName(String appName){
        long size = searchSizeByAppName(appName);
        if(size == 0){
            return null;
        }
        return redisTemplate.opsForList().index(appName,size);
    }

    /**
     * 当一个worker故障时，删除所有的关于其的定时任务
     * @param appName
     */
    @Transactional
    public void deleteAllByAppName(String appName){
        redisTemplate.opsForList().remove(appName,0,serchLastScheduleIdByAppName(appName));
    }

    /**
     * 选择database
     * @param selectDataBase
     */
    private void selectDataBase(int selectDataBase){
        if(selectDataBase >= 0 && selectDataBase <= 16) {
            RedisConnectionFactory redisConnectionFactory = redisTemplate.getConnectionFactory();
            RedisConnection redisConnection = redisConnectionFactory.getConnection();
            redisConnection.select(selectDataBase);
        }else{
            log.error("切换的database不在0-16之间");
        }
    }

    public boolean insertAllParams(String appName,List<Integer> params){
        try {
            for(Integer param : params){
                insertScheduleByAppName(appName,param);
            }
            return true;
        }catch (Exception e){
            log.error("批量插入失败",e);
            return false;
        }
    }

}
