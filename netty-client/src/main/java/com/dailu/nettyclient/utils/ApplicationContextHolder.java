package com.dailu.nettyclient.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class ApplicationContextHolder implements ApplicationContextAware, InitializingBean {
    public static ApplicationContext applicationContext;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (ApplicationContextHolder.applicationContext == null) {
            ApplicationContextHolder.setContext(applicationContext);
        }
    }

    public static <T> Optional<T> getBean(Class<T> tClass) {
        return Optional.of(applicationContext.getBean(tClass));
    }


    public static <T> T requireBean(Class<T> tClass) {
        return applicationContext.getBean(tClass);
    }

    public static ObjectMapper getObjectMapper() {
        return getBean(ObjectMapper.class).orElse(objectMapper);
    }

    @Override
    public void afterPropertiesSet() {
        log.debug("applicationContextHolder初始化......");
    }


    public static void setContext(ApplicationContext applicationContext) {
        ApplicationContextHolder.applicationContext = applicationContext;
    }
}