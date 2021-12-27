package com.dailu.nettyclient.config;

import com.dailu.nettyclient.aspect.annotation.RpcClient;
import com.dailu.nettyclient.factory.RpcClientFactoryBean;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ObjectUtils;

import java.util.Map;
import java.util.Set;

public class RpcInitConfig implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        ClassPathScanningCandidateComponentProvider provider = getScanner();
        //设置扫描器
        provider.addIncludeFilter(new AnnotationTypeFilter(RpcClient.class));
        //扫描此包下的所有带有@RpcClient的注解的类
        Set<BeanDefinition> beanDefinitionSet = provider.findCandidateComponents("com.dailu.nettyclient.rpc");
        for (BeanDefinition beanDefinition : beanDefinitionSet) {
            if (beanDefinition instanceof AnnotatedBeanDefinition) {
                //获得注解上的参数信息
                AnnotatedBeanDefinition annotatedBeanDefinition = (AnnotatedBeanDefinition) beanDefinition;
                String beanClassAllName = beanDefinition.getBeanClassName();
                Map<String, Object> paraMap = annotatedBeanDefinition.getMetadata()
                        .getAnnotationAttributes(RpcClient.class.getCanonicalName());
                //将RpcClient的工厂类注册进去
                BeanDefinitionBuilder builder = BeanDefinitionBuilder
                        .genericBeanDefinition(RpcClientFactoryBean.class);
                //设置RpcClientFactoryBean工厂类中的构造函数的值
                //这里的string className会变成Class<?>赋给RpcClientFactoryBean构造函数
                builder.addConstructorArgValue(beanClassAllName);
                if(!ObjectUtils.isEmpty(paraMap)){
                    builder.addConstructorArgValue(paraMap.getOrDefault("destiny",""));
                }
                builder.getBeanDefinition().setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
                //将其注册进容器中
                registry.registerBeanDefinition(
                        beanClassAllName,
                        builder.getBeanDefinition());
            }
        }
    }

    //允许Spring扫描接口上的注解
    private ClassPathScanningCandidateComponentProvider getScanner() {
        return new ClassPathScanningCandidateComponentProvider(false) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
            }
        };
    }
}