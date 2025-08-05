package com.dailu.nettyclient.proxy;

import com.dailu.nettyclient.config.ClientInitConfig;
import com.dailu.nettyclient.exception.CustomException;
import com.dailu.nettyclient.utils.ApplicationContextHolder;
import com.dailu.nettycommon.dto.RequestInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;


@Slf4j
@RequiredArgsConstructor
public class DynamicServiceProxy implements MethodInterceptor {

    private final String targetClass;


    @Override
    public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        if (method.getName().equals("toString")) {
            return method.invoke(o, args);
        }
        ObjectMapper objectMapper = ApplicationContextHolder.getObjectMapper();
        //组装传输类的属性值
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setClassName(targetClass);
        requestInfo.setMethodName(method.getName());
        Class<?>[] parameterTypes = method.getParameterTypes();
        requestInfo.setParamTypes(parameterTypes);
        requestInfo.setParams(args);
        String result;
        try {
            result = ClientInitConfig.nettyClientHandler.send(requestInfo);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw CustomException.wrap(e.getMessage(),e);
        }
        Type returnType = method.getAnnotatedReturnType().getType();
        if (ObjectUtils.isEmpty(result)) {
            return ((Class<?>) returnType).newInstance();
        }
        return objectMapper.readValue(result, (Class<?>) returnType);
    }

}
