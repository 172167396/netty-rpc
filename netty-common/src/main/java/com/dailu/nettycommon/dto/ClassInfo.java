package com.dailu.nettycommon.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * rpc调用时传输类的信息
 * 客户端与服务端之间通信，传递信息的媒介
 */
@Getter
@Setter
public class ClassInfo {
    //自定义name，一般一个接口有多个实现类的时候使用自定义
    // 或者默认使用接口名称
    private String name;
    private String methodName;
    //参数类型
    private Class<?>[] types;
    //参数列表
    private Object[] params;
    //自定义rpc协议
    private String protocol="#rpc#";

}
