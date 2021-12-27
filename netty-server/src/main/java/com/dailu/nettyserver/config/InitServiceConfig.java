package com.dailu.nettyserver.config;

import com.dailu.nettycommon.propertiy.ClientBootStrap;
import com.dailu.nettyserver.serve.NettyServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import java.lang.reflect.Method;
import java.util.*;

import static com.dailu.nettyserver.serve.ApplicationContextHolder.applicationContext;

@Slf4j
@Component
public class InitServiceConfig implements CommandLineRunner {

    public static Map<String, Object> serviceMap = new HashMap<>();
    public static Map<String, Method> uriMap = new HashMap<>();

    @Async
    @Override
    public void run(String... args) {
        Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(Service.class);
        for (Object bean : beansWithAnnotation.values()) {
            Class<?> clazz = bean.getClass();
            Class<?>[] interfaces = clazz.getInterfaces();
            for (Class<?> inter : interfaces) {
                serviceMap.put(getClassName(inter.getName()), bean);
                log.info("已经加载的服务:" + inter.getName());
            }
        }

        Map<String, Object> beansWithRequestMapping = applicationContext.getBeansWithAnnotation(RestController.class);
        for (Object bean : beansWithRequestMapping.values()) {
            Class<?> clazz = bean.getClass();
            Method[] declaredMethods = clazz.getDeclaredMethods();
            for(Method m : declaredMethods){
                if(m.isAnnotationPresent(GetMapping.class)){
                    GetMapping get = m.getDeclaredAnnotation(GetMapping.class);
                    String[] value = get.value();
                    uriMap.put(value[0],m);
                }
                if(m.isAnnotationPresent(PostMapping.class)){
                    PostMapping post = m.getDeclaredAnnotation(PostMapping.class);
                    String[] value = post.value();
                    uriMap.put(value[0],m);
                }
            }

        }
        NettyServer.start(ClientBootStrap.getPort());
        log.info("netty server 已启动.........");
    }

    private String getClassName(String beanClassName) {
        String className = beanClassName.substring(beanClassName.lastIndexOf(".") + 1);
        className = className.substring(0, 1).toLowerCase() + className.substring(1);
        return className;
    }
}
