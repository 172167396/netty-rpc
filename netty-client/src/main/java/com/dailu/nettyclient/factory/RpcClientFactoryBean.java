package com.dailu.nettyclient.factory;

import com.dailu.nettyclient.proxy.DynamicServiceProxy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.cglib.proxy.Enhancer;


@RequiredArgsConstructor
public class RpcClientFactoryBean implements FactoryBean<Object> {

    private final Class<?> classType;

    private final String destiny;

    @Override
    public Object getObject() {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(classType);
        enhancer.setCallback(new DynamicServiceProxy(destiny));
        return enhancer.create();
    }


    @Override
    public Class<?> getObjectType() {
        return classType;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}