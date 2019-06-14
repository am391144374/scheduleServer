package com.l.scheduleserver.conf.initZookeeper;

import com.l.scheduleserver.bean.ScheduleBean;
import com.l.scheduleserver.bean.WorkerServiceInfo;
import com.l.scheduleserver.conf.annotation.Schedule;
import com.l.scheduleserver.conf.annotation.ScheduleGetBeanFromMethod;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class ScheduleInit {

    private String Path = "com.l.scheduleserver";

    public void scanPacketToGetScheduleBean(){
        log.info("扫描添加定时任务开始！");
        try {
            String packagePath = Path.replace(".", File.separator);
            Enumeration<URL> dirs = Thread.currentThread().getContextClassLoader().getResources(packagePath);
            while (dirs.hasMoreElements()){
                URL url = dirs.nextElement();
                String protocol = url.getProtocol();
                if("file".equals(protocol)){
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    findAndAddScheduleClass(filePath,packagePath);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void findAndAddScheduleClass(String filePath,String packagePath){
        File parentFile = new File(filePath);
        if(!parentFile.exists()){
            return;
        }
        File[] files = parentFile.listFiles(file -> (parentFile.isDirectory()) || parentFile.getName().endsWith(".class"));
        for(File file : files){
            if(file.isDirectory()){
                findAndAddScheduleClass(file.getPath(),packagePath);
            }else{
                String filePackagePath = file.getPath();
                int lastPackageIndex = filePackagePath.lastIndexOf(packagePath);
                String lastClassName = filePackagePath.substring(lastPackageIndex).replace(File.separator,".");
                scanScheduleClass(lastClassName.substring(0,lastClassName.length() - 6));
            }
        }
    }

    private void scanScheduleClass(String classPath){
        try {
            Class schClass = Class.forName(classPath);
            Schedule annotation = (Schedule) schClass.getAnnotation(Schedule.class);
            if(annotation == null){
                return;
            }
            if(!annotation.start()){
                return;
            }
            Method[] methods = schClass.getMethods();
            for(Method method : methods){
                if(method.getAnnotation(ScheduleGetBeanFromMethod.class) == null){
                    continue;
                }
                ScheduleBean scheduleBean = (ScheduleBean) method.invoke(schClass.newInstance());
                log.info("扫描----添加定时任务：{}",scheduleBean.getScheduleName());
                if(!WorkerServiceInfo.putWork(scheduleBean)){
                    log.error("存在重复的定时任务ID：{}",scheduleBean.getScheduleId());
                }
            }
        } catch (ClassNotFoundException e) {
            log.error("ClassNotFoundException----",e);
        } catch (IllegalAccessException e) {
            log.error("IllegalAccessException----",e);
        } catch (InstantiationException e) {
            log.error("InstantiationException----",e);
        } catch (InvocationTargetException e) {
            log.error("InvocationTargetException----",e);
        }
    }

}
