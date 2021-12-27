package com.dailu.nettyclient.config;

import com.dailu.nettyclient.handler.NettyClientHandler;
import com.dailu.nettycommon.decoder.MyMessageDecoder;
import com.dailu.nettycommon.encoder.MyMessageEncoder;
import com.dailu.nettycommon.propertiy.ClientBootStrap;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;

@Slf4j
@Configuration
public class ClientInitConfig implements CommandLineRunner {

    public static NettyClientHandler nettyClientHandler;

    @Async
    @Override
    public void run(String... args) {
        nettyClientHandler = new NettyClientHandler();
        //客户端需要一个事件循环组就可以
        NioEventLoopGroup group = new NioEventLoopGroup(1);
        try {
            //创建客户端的启动对象 bootstrap ，不是 serverBootStrap
            Bootstrap bootstrap = new Bootstrap();
            //设置相关参数
            bootstrap.group(group) //设置线程组
                    .channel(NioSocketChannel.class) //设置客户端通道的实现数 （反射）
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                    .addLast(new MyMessageDecoder())
                                    .addLast(new MyMessageEncoder())
                                    .addLast(nettyClientHandler); //加入自己的处理器
                        }
                    });
            log.info("client is ready!");
            //连接服务器
            final ChannelFuture channelFuture = bootstrap.connect(ClientBootStrap.getHost(), ClientBootStrap.getPort()).sync();
            //对关闭通道进行监听
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}
