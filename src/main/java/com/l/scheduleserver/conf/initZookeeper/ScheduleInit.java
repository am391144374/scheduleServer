package com.l.scheduleserver.conf.initZookeeper;

import com.l.scheduleserver.bean.ScheduleBean;
import com.l.scheduleserver.bean.WorkerServiceInfo;
import com.l.scheduleserver.conf.annotation.Schedule;
import com.l.scheduleserver.conf.annotation.ScheduleGetBeanFromMethod;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

public class ScheduleInit {

    private String Path = "com.l.scheduleserver";
    private Set<String> scheduleClassPath = new HashSet<>();

    public void scanPacketToGetScheduleBean(){
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
                scheduleClassPath.add(lastClassName.substring(0,lastClassName.length() - 6));
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
                WorkerServiceInfo.putWork(scheduleBean);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}
