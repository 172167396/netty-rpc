package com.dailu.nettyclient.handler;

import com.dailu.nettyclient.exception.CustomerException;
import com.dailu.nettyclient.utils.ApplicationContextHolder;
import com.dailu.nettyclient.utils.BlockedQueue;
import com.dailu.nettycommon.dto.RequestInfo;
import com.dailu.nettycommon.dto.ResponseInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.TaskScheduler;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    private static ChannelHandlerContext context;

    private final ConcurrentHashMap<String, BlockedQueue<ResponseInfo>> queueMap = new ConcurrentHashMap<>();

    /**
     * 通道连接时，就将上下文保存下来，因为这样其他函数也可以用
     */
    @Override
    public synchronized void channelActive(ChannelHandlerContext ctx) {
        log.debug("client channel is active..........");
        context = ctx;
        Optional<TaskScheduler> taskScheduler = ApplicationContextHolder.getBean(TaskScheduler.class);
        taskScheduler.ifPresent(scheduler -> scheduler.scheduleAtFixedRate(() -> {
            Iterator<Map.Entry<String, BlockedQueue<ResponseInfo>>> iterator = queueMap.entrySet().iterator();
            while(iterator.hasNext()) {
                Map.Entry<String, BlockedQueue<ResponseInfo>> entry = iterator.next();
                BlockedQueue<ResponseInfo> expiredQueue = entry.getValue();
                if (expiredQueue.isExpired()) {
                    iterator.remove();
                    log.info("requestId:{},响应内容:{},已超时，已从map中移除", entry.getKey(), expiredQueue.poll(1));
                }
            }
        }, Duration.of(10, ChronoUnit.SECONDS)));
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
            queueMap.computeIfPresent(responseInfo.getRequestId(), (s, blockedQueue) -> {
                blockedQueue.add(responseInfo);
                return blockedQueue;
            });
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
            ObjectMapper objectMapper = ApplicationContextHolder.getObjectMapper();
            String s = objectMapper.writeValueAsString(requestInfo);
            queueMap.putIfAbsent(requestInfo.getUuid(), new BlockedQueue<>());
            context.writeAndFlush(s);
            log.info("client发出数据:" + s);
            ResponseInfo responseInfo = takeRpcResponse(requestInfo.getUuid());
            if (responseInfo == null) {
                return null;
            }
            return objectMapper.writeValueAsString(responseInfo.getResult());
        } catch (JsonProcessingException e) {
            throw CustomerException.wrap(e.getMessage(), e);
        }
    }

    /**
     * 获取响应结果,默认超时时间30秒,超过30秒抛出AcquireResultTimeoutException
     *
     * @param requestId 请求ID
     */
    @Nullable
    public ResponseInfo takeRpcResponse(String requestId) {
        ResponseInfo responseInfo = queueMap.get(requestId).poll();
        queueMap.remove(requestId);
        return responseInfo;
    }
}
