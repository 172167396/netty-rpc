package com.dailu.nettyserver.handler;

import com.dailu.nettycommon.dto.ClassInfo;
import com.dailu.nettyserver.config.InitServiceConfig;
import com.dailu.nettyserver.serve.ApplicationContextHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

@Slf4j
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            String s = (String) msg;
            log.info("server端收到数据：" + s);
            ObjectMapper objectMapper = ApplicationContextHolder.getBean(ObjectMapper.class).orElseGet(ObjectMapper::new);
            ClassInfo classInfo = objectMapper.readValue(s, ClassInfo.class);
            //确认是rpc调用才往下执行
            if (classInfo != null && "#rpc#".equals(classInfo.getProtocol())) {
                //反射调用实现类的方法
                String name = classInfo.getName();
                Object service = InitServiceConfig.serviceMap.get(name);
                Method method = service.getClass().getDeclaredMethod(classInfo.getMethodName(), classInfo.getTypes());
                Object result = method.invoke(service, classInfo.getParams());
                String resultStr = objectMapper.writeValueAsString(result);
                ctx.writeAndFlush(resultStr);
                log.info("server端返回："+resultStr);
            }
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            ctx.writeAndFlush("");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(cause.getMessage(),cause);
        ctx.close();
    }


}
