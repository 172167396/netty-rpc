package com.dailu.nettyserver.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ChannelHandler.Sharable
public class NettyOutBoundHandler extends ChannelOutboundHandlerAdapter {

    public static Map<String,ChannelHandlerContext> clientMap = new ConcurrentHashMap<>();


    @Override
    public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        log.debug("NettyOutBoundHandler bind........");
        super.bind(ctx, localAddress, promise);
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        log.debug("NettyOutBoundHandler connect........");
        super.connect(ctx, remoteAddress, localAddress, promise);
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        log.debug("NettyOutBoundHandler disconnect........");
        super.disconnect(ctx, promise);
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        log.debug("NettyOutBoundHandler close........");
        super.close(ctx, promise);
    }

    @Override
    public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        log.debug("NettyOutBoundHandler deregister........");
        super.deregister(ctx, promise);
    }

    @Override
    public void read(ChannelHandlerContext ctx) throws Exception {
        log.debug("NettyOutBoundHandler read........");
        super.read(ctx);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        String message = (String) msg;
        log.debug("outBoundHandler1 receive "+message);
        ctx.write(message+"outBoundHandler1 is complete too!", promise);
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        log.debug("NettyOutBoundHandler flush........");
        super.flush(ctx);
    }

}
