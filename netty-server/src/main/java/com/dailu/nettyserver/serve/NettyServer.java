package com.dailu.nettyserver.serve;

import com.dailu.nettycommon.decoder.MyMessageDecoder;
import com.dailu.nettycommon.encoder.MyMessageEncoder;
import com.dailu.nettyserver.handler.NettyOutBoundHandler;
import com.dailu.nettyserver.handler.NettyOutBoundHandler2;
import com.dailu.nettyserver.handler.NettyServerHandler;
import com.dailu.nettyserver.handler.NettyServerHandler2;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyServer {

    //启动netty服务端
    public static void start(int port) {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        final NettyServerHandler nettyServerHandler = new NettyServerHandler();
        final NettyServerHandler2 nettyServerHandler2 = new NettyServerHandler2();
        final NettyOutBoundHandler nettyOutBoundHandler = new NettyOutBoundHandler();
        final NettyOutBoundHandler2 nettyOutBoundHandler2 = new NettyOutBoundHandler2();
        try {
            //创建服务端的启动对象，并使用链式编程来设置参数
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup) //设置两个线程组
                    .channel(NioServerSocketChannel.class)//使用NioServerSocketChannel 作为服务器的通道实现
                    .option(ChannelOption.SO_BACKLOG, 128)//设置线程队列的连接个数
                    .childOption(ChannelOption.SO_KEEPALIVE, true) //设置一直保持活动连接状态
                    .childHandler(new ChannelInitializer<SocketChannel>() {//设置一个通道测试对象
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            //给pipeline设置通道处理器
                            ch.pipeline()
                                    .addLast(new MyMessageDecoder())
                                    .addLast(new MyMessageEncoder())
                                    //多handler,out在前倒叙，in在后正序
//                                    .addLast(nettyOutBoundHandler2)
//                                    .addLast(nettyOutBoundHandler)
                                    .addLast(nettyServerHandler);
//                                    .addLast(nettyServerHandler2);
                            log.info(ch.remoteAddress() + "已经连接上");
                        }
                    });//给 workerGroup 的EventLoop对应的管道设置处理器
            //启动服务器，并绑定端口并且同步
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();

            //给 channelFuture 注册监听器，监听关心的事件,异步的时候使用
            channelFuture.addListener((future) -> {
                if (future.isSuccess()) {
                    System.out.println("监听端口" + port + "成功.......");
                } else {
                    System.out.println("监听端口" + port + "失败......");
                }
            });
            //对关闭通道进行监听,监听到通道关闭后，往下执行
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.debug("server端中断，{}",e.getMessage(),e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
