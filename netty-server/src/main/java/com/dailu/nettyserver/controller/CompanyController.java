package com.dailu.nettyserver.controller;

import com.dailu.nettyserver.config.InitServiceConfig;
import com.dailu.nettyserver.serve.ApplicationContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@RestController
public class CompanyController {

    @GetMapping("/getCompany")
    public String getCompany(String id){
        return "can not find:"+id;
    }

    @GetMapping("/invoke")
    public String invoke(String path,String param) throws InvocationTargetException, IllegalAccessException {
        Method method = InitServiceConfig.uriMap.get(path);
        if(method != null){
            Object o = ApplicationContextHolder.requireBean(method.getDeclaringClass());
            return method.invoke(o,param).toString();
        }
        return "path not found";
    }


}
