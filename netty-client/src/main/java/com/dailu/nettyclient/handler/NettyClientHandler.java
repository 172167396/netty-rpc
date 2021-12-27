package com.dailu.nettyclient.handler;

import com.dailu.nettyclient.utils.ApplicationContextHolder;
import com.dailu.nettycommon.dto.ClassInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 由于需要在 handler 中发送消息给服务端，并且将服务端返回的消息读取后返回给消费者
 * 所以实现了 Callable 接口，这样可以运行有返回值的线程
 */
@Slf4j
@Setter
public class NettyClientHandler extends ChannelInboundHandlerAdapter implements Callable<String> {

    private ChannelHandlerContext context;
    private ClassInfo classInfo;


    /**
     *服务端返回的结果
     */
    private String result;
    /**
     * 使用锁将 channelRead和 execute 函数同步
     */
    private final Lock lock = new ReentrantLock();
    /**
     * 精准唤醒 execute中的等待
     */
    private final Condition condition = lock.newCondition();


    //通道连接时，就将上下文保存下来，因为这样其他函数也可以用
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.context = ctx;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("channelInactive 被调用。。。");
    }

    //当服务端返回消息时，将消息复制到类变量中，然后唤醒正在等待结果的线程，返回结果
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        lock.lock();
        log.info("channel hashCode:" + ctx.channel().hashCode());
        log.info("收到服务端发送的消息:" + msg);
        result = msg.toString();
        //唤醒等待的线程
        condition.signal();
        lock.unlock();
    }

    public String execute(ClassInfo classInfo) throws Exception {
        lock.lock();
        final String s = ApplicationContextHolder.getBean(ObjectMapper.class).orElseGet(ObjectMapper::new).writeValueAsString(classInfo);
        context.writeAndFlush(s);
        log.info("client发出数据:" + s);
        //向服务端发送消息后等待channelRead中接收到消息后唤醒
        condition.await();
        lock.unlock();
        return result;
    }

    //异常处理
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(cause.getMessage(),cause);
    }

    @Override
    public String call() throws Exception {
        lock.lock();
        final String s = ApplicationContextHolder.getBean(ObjectMapper.class)
                .orElseGet(ObjectMapper::new).writeValueAsString(classInfo);
        context.writeAndFlush(s);
        log.info("client发出数据:" + s);
        //向服务端发送消息后等待channelRead中接收到消息后唤醒
        condition.await();
        lock.unlock();
        return result;
    }
}
