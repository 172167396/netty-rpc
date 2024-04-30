package com.dailu.nettyclient.aspect.annotation;

import com.dailu.nettyclient.config.CustomBeanDefinitionRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = {ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import(CustomBeanDefinitionRegistrar.class)
public @interface EnableRpcClient {
}
