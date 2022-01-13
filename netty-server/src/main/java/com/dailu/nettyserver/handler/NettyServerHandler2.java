package com.dailu.nettyserver.handler;

import com.dailu.nettycommon.dto.ClassInfo;
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
public class NettyServerHandler2 extends ChannelInboundHandlerAdapter {

    public static Map<String,ChannelHandlerContext> clientMap = new ConcurrentHashMap<>();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        SocketAddress socketAddress = ctx.channel().remoteAddress();
        log.debug("channel2 active..........");
        log.debug("remote address is " + socketAddress.toString());
        log.debug("client2 channel id is " + ctx.channel().id());
        clientMap.put(socketAddress.toString(),ctx);
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
        log.debug("client：{}断开连接",ctx.channel().id());
        clientMap.remove(ctx.channel().remoteAddress().toString());
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
            String s = (String) msg;
            log.info("handler2 receive " + s);
            ctx.write(s+"handler 2 is complete!");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(cause.getMessage(), cause);
        ctx.close();
    }


}
