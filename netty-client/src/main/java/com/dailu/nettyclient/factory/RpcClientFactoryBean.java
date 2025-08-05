package com.dailu.nettyclient.factory;

import com.dailu.nettyclient.proxy.DynamicServiceProxy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.cglib.proxy.Enhancer;

import java.util.HashMap;
import java.util.Map;


@RequiredArgsConstructor
public class RpcClientFactoryBean implements FactoryBean<Object> {

    private final Class<?> classType;

    private final String destiny;

    private final Map<Class<?>, Object> cache = new HashMap<>();


    @Override
    public synchronized Object getObject() {
        return cache.computeIfAbsent(classType, clazz -> {
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(classType);
            enhancer.setCallback(new DynamicServiceProxy(destiny));
            return enhancer.create();
        });
    }


    @Override
    public Class<?> getObjectType() {
        return classType;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}