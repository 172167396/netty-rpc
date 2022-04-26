package com.dailu.nettycommon.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * rpc调用时传输类的信息
 * 客户端与服务端之间通信，传递信息的媒介
 */
@Getter
@Setter
public class RequestInfo {
    private String uuid = UUID.randomUUID().toString();
    /**
     * 调用类名
     */
    private String className;
    /**
     * 方法名称
     */
    private String methodName;
    /**
     *参数类型
     */
    private Class<?>[] paramTypes;
    /**
     *参数列表
     */
    private Object[] params;
    /**
     * 自定义rpc协议
     */
    private String protocol="#rpc#";


}
