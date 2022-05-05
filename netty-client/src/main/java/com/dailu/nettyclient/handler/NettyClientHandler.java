package com.dailu.nettyclient.handler;

import com.dailu.nettyclient.utils.ApplicationContextHolder;
import com.dailu.nettyclient.utils.BlockedQueue;
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

@Slf4j
public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    public static ChannelHandlerContext context;

    private final Map<String, BlockedQueue<ResponseInfo>> futureMap = new ConcurrentHashMap<>();


    /**
     * 通道连接时，就将上下文保存下来，因为这样其他函数也可以用
     */
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
            ResponseInfo responseInfo = ApplicationContextHolder.getObjectMapper().readValue(msg.toString(), ResponseInfo.class);
            futureMap.get(responseInfo.getRequestId()).add(responseInfo);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
        }
    }


    //异常处理
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(cause.getMessage(), cause);
    }


    public String send(RequestInfo requestInfo) {
        try {
            //测试单个请求阻塞
            if ("user001".equals(requestInfo.getParams()[0])) {
                Thread.sleep(10000);
            }
            ObjectMapper objectMapper = ApplicationContextHolder.getObjectMapper();
            String s = objectMapper.writeValueAsString(requestInfo);
            futureMap.putIfAbsent(requestInfo.getUuid(), new BlockedQueue<>());
            context.writeAndFlush(s);
            log.info("client发出数据:" + s);
            Object result = getRpcResponse(requestInfo.getUuid()).getResult();
            return objectMapper.writeValueAsString(result);
        } catch (InterruptedException | JsonProcessingException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public ResponseInfo getRpcResponse(String requestId) {
        try {
            return futureMap.get(requestId).poll();
        } finally {
            futureMap.remove(requestId);
        }
    }
}
