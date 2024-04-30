package com.dailu.nettyserver.handler;

import com.dailu.nettycommon.dto.RequestInfo;
import com.dailu.nettycommon.dto.ResponseInfo;
import com.dailu.nettyserver.config.InitServiceConfig;
import com.dailu.nettyserver.serve.ApplicationContextHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ChannelHandler.Sharable
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    public static Map<String, ChannelHandlerContext> clientMap = new ConcurrentHashMap<>();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        AttributeKey<String> attributeKey = AttributeKey.valueOf("clientId");
        String s = ctx.channel().attr(attributeKey).get();
        SocketAddress socketAddress = ctx.channel().remoteAddress();
        log.debug("channel active..........");
        log.debug("remote address is " + socketAddress.toString());
        log.debug("client channel id is " + ctx.channel().id());
        clientMap.put(socketAddress.toString(), ctx);
        super.channelActive(ctx);
    }

    /**
     * 触发场景
     * 1.客户端发送关闭帧
     * 2.客户端结束进程
     * 3.服务端主动调用channel.close()
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.debug("client：{}断开连接", ctx.channel().id());
        clientMap.remove(ctx.channel().remoteAddress().toString());
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            String s = (String) msg;
            log.debug("handler 1 received " + s);
            ObjectMapper objectMapper = ApplicationContextHolder.getObjectMapper();
            RequestInfo requestInfo = objectMapper.readValue(s, RequestInfo.class);
            //确认是rpc调用才往下执行
            if (requestInfo != null && "#rpc#".equals(requestInfo.getProtocol())) {
                //反射调用实现类的方法
                String name = requestInfo.getClassName();
                Object service = InitServiceConfig.serviceMap.get(name);
                Method method = service.getClass().getDeclaredMethod(requestInfo.getMethodName(), requestInfo.getParamTypes());
                Object result = method.invoke(service, requestInfo.getParams());
                String response = objectMapper.writeValueAsString(new ResponseInfo(requestInfo.getUuid(),result));
                ctx.writeAndFlush(response);
                log.info("server端返回：" + response);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ctx.writeAndFlush("");
        }
//        ctx.fireChannelRead("handler1 is complete!");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(cause.getMessage(), cause);
        ctx.close();
    }


}
