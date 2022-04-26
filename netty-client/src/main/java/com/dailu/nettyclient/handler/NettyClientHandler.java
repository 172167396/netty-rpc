package com.dailu.nettyclient.handler;

import com.dailu.nettyclient.utils.ApplicationContextHolder;
import com.dailu.nettyclient.utils.DefaultFuture;
import com.dailu.nettycommon.dto.RequestInfo;
import com.dailu.nettycommon.dto.ResponseInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 由于需要在 handler 中发送消息给服务端，并且将服务端返回的消息读取后返回给消费者
 * 所以实现了 Callable 接口，这样可以运行有返回值的线程
 */
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    public static ChannelHandlerContext context;

    private final Map<String, DefaultFuture> futureMap = new ConcurrentHashMap<>();

    /**
     * 使用锁将 channelRead和 execute 函数同步
     */
    private ReentrantLock lock = new ReentrantLock();
    /**
     * 精准唤醒 execute中的等待
     */
    private Condition condition = lock.newCondition();


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
        log.info("收到服务端发送的消息:" + msg);
        try {
            ResponseInfo responseInfo = new ObjectMapper().readValue(msg.toString(), ResponseInfo.class);
            futureMap.get(responseInfo.getRequestId()).setResponse(responseInfo);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }


    //异常处理
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(cause.getMessage(), cause);
    }


    public String send(RequestInfo requestInfo) {
        try {
            if ("user001".equals(requestInfo.getParams()[0])) {
                Thread.sleep(10000);
            }
            String s = ApplicationContextHolder.getObjectMapper().writeValueAsString(requestInfo);
            futureMap.putIfAbsent(requestInfo.getUuid(), new DefaultFuture());
            context.writeAndFlush(s).await();
            log.info("client发出数据:" + s);
            Object result = getRpcResponse(requestInfo.getUuid()).getResult();
            return ApplicationContextHolder.getObjectMapper().writeValueAsString(result);
        } catch (InterruptedException | JsonProcessingException e) {
            e.printStackTrace();
            return "";
        }
    }

    public ResponseInfo getRpcResponse(String requestId) {
        try {
            DefaultFuture future = futureMap.get(requestId);
            return future.getRpcResponse(5000);
        } finally {
            //获取成功以后，从map中移除
            futureMap.remove(requestId);
        }
    }
}
