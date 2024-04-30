package com.dailu.nettyclient.factory;

import com.dailu.nettyclient.proxy.DynamicServiceProxy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.cglib.proxy.Enhancer;


@RequiredArgsConstructor
public class RpcClientFactoryBean implements FactoryBean<Object> {

    private final Class<?> classType;

    private final String destiny;

    private Object instance = null;


    @Override
    public synchronized Object getObject() {
        if (instance != null) {
            return instance;
        }
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(classType);
        enhancer.setCallback(new DynamicServiceProxy(destiny));
        instance = enhancer.create();
        return instance;
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