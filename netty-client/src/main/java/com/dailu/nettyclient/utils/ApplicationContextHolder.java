package com.dailu.nettyclient.utils;
 
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ApplicationContextHolder implements ApplicationContextAware, InitializingBean {
    public static ApplicationContext applicationContext;

    @Override  
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {  
        if (ApplicationContextHolder.applicationContext == null) {
            ApplicationContextHolder.applicationContext = applicationContext;
        }
    }

    public static <T> Optional<T> getBean(Class<T> tClass){
        return Optional.of(applicationContext.getBean(tClass));
    }


    public static <T> T requireBean(Class<T> tClass){
        return applicationContext.getBean(tClass);
    }
    public static ObjectMapper getObjectMapper(){
        return getBean(ObjectMapper.class).orElseGet(ObjectMapper::new);
    }

    @Override
    public void afterPropertiesSet() {
        System.out.println("applicationContextHolder初始化......");
    }

    public static void main(String[] args) {
        ConcurrentHashMap<String,String> map = new ConcurrentHashMap<>();
        map.put("3Qj","1");
        map.put("2pj","2");

        Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            System.out.println(entry.getKey());
            System.out.println(entry.getValue());
        }

    }

}