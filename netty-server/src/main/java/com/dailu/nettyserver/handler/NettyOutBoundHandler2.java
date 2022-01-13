package com.dailu.nettyserver.handler;

import io.netty.channel.*;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ChannelHandler.Sharable
public class NettyOutBoundHandler2 extends ChannelOutboundHandlerAdapter {

    public static Map<String,ChannelHandlerContext> clientMap = new ConcurrentHashMap<>();


    @Override
    public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        log.debug("outBoundHandler2 bind........");
        super.bind(ctx, localAddress, promise);
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        log.debug("outBoundHandler2 connect........");
        super.connect(ctx, remoteAddress, localAddress, promise);
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        log.debug("outBoundHandler2 disconnect........");
        super.disconnect(ctx, promise);
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        log.debug("outBoundHandler2 close........");
        super.close(ctx, promise);
    }

    @Override
    public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        log.debug("outBoundHandler2 deregister........");
        super.deregister(ctx, promise);
    }

    @Override
    public void read(ChannelHandlerContext ctx) throws Exception {
        log.debug("outBoundHandler2 read........");
        super.read(ctx);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        String s = (String)msg;
        log.debug("outBoundHandler2 receive "+s);
        promise.addListener(future -> {
            System.out.println("promise done..........future is "+future);
        });
        ctx.writeAndFlush(s+"outBoundHandler2 finish!",promise);
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        log.debug("outBoundHandler2 flush........");
        super.flush(ctx);
    }
}
