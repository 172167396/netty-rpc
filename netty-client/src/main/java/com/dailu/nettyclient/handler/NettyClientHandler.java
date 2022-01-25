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
public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    public static ChannelHandlerContext context;
    /**
     * 服务端返回的结果
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
        log.debug("client channel is active..........");
        context = ctx;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.debug("client shutdown.......");
        super.channelInactive(ctx);
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

    public String execute(ClassInfo classInfo) {
        lock.lock();
        try {
            if ("user001".equals(classInfo.getParams()[0])) {
                throw new RuntimeException();
            }
            String s = ApplicationContextHolder.getBean(ObjectMapper.class)
                    .orElseGet(ObjectMapper::new).writeValueAsString(classInfo);
            context.writeAndFlush(s);
            log.info("client发出数据:" + s);
            //向服务端发送消息后等待channelRead中接收到消息后唤醒
            condition.await();
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    public String send(String msg) throws Exception {
        lock.lock();
        context.writeAndFlush(msg);
        log.info("client发出数据:" + msg);
        //向服务端发送消息后等待channelRead中接收到消息后唤醒
        condition.await();
        lock.unlock();
        return result;
    }

    //异常处理
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(cause.getMessage(), cause);
    }

}
