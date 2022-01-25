package com.dailu.nettyclient.proxy;

import com.dailu.nettyclient.utils.ThreadPoolUtil;
import com.dailu.nettycommon.dto.ClassInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.concurrent.Future;

import static com.dailu.nettyclient.config.ClientInitConfig.nettyClientHandler;

@Slf4j
@RequiredArgsConstructor
public class DynamicServiceProxy implements MethodInterceptor {

    private final String targetClass;

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        if (method.getName().equals("toString")) {
            return method.invoke(o, objects);
        }
        //组装传输类的属性值
        ClassInfo classInfo = new ClassInfo();
        classInfo.setName(targetClass);
        classInfo.setMethodName(method.getName());
        Class<?>[] parameterTypes = method.getParameterTypes();
        classInfo.setTypes(parameterTypes);
        classInfo.setParams(objects);
        String result;
        try {
            result = nettyClientHandler.execute(classInfo);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            Class<?> type = (Class<?>) method.getAnnotatedReturnType().getType();
            return type.newInstance();
        }
        Type returnType = method.getAnnotatedReturnType().getType();
        return new ObjectMapper().readValue(result, (Class<?>) returnType);
    }

}
