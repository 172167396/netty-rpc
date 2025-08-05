package com.dailu.nettyclient.handler;

import com.dailu.nettyclient.exception.CustomException;
import com.dailu.nettyclient.model.dto.CompletableFutureWrapper;
import com.dailu.nettyclient.utils.ApplicationContextHolder;
import com.dailu.nettycommon.dto.RequestInfo;
import com.dailu.nettycommon.dto.ResponseInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.TaskScheduler;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    private final ObjectMapper objectMapper;

    private static ChannelHandlerContext context;


    private final ConcurrentHashMap<String, CompletableFutureWrapper<ResponseInfo>> queueMap = new ConcurrentHashMap<>();

    /**
     * 通道连接时，就将上下文保存下来，因为这样其他函数也可以用
     */
    @Override
    public synchronized void channelActive(ChannelHandlerContext ctx) {
        log.debug("client channel is active..........");
        context = ctx;
        Optional<TaskScheduler> taskScheduler = ApplicationContextHolder.getBean(TaskScheduler.class);
        taskScheduler.ifPresent(scheduler -> scheduler.scheduleAtFixedRate(() -> {
            Iterator<Map.Entry<String, CompletableFutureWrapper<ResponseInfo>>> iterator = queueMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, CompletableFutureWrapper<ResponseInfo>> entry = iterator.next();
                CompletableFutureWrapper<ResponseInfo> futureWrapper = entry.getValue();
                if (futureWrapper.isExpired()) {
                    futureWrapper.clear();
                    iterator.remove();
                    log.info("requestId:{},已超时，已从map中移除", entry.getKey());
                }
            }
        }, Duration.of(10, ChronoUnit.SECONDS)));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.debug("client shutdown.......");
        super.channelInactive(ctx);
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        log.info("收到服务端发送的消息:" + msg);
        try {
            ResponseInfo responseInfo = ApplicationContextHolder.getObjectMapper().readValue(msg.toString(), ResponseInfo.class);
            queueMap.computeIfPresent(responseInfo.getRequestId(), (s, futureWrapper) -> {
                futureWrapper.complete(responseInfo);
                return futureWrapper;
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
            String s = objectMapper.writeValueAsString(requestInfo);
            queueMap.putIfAbsent(requestInfo.getUuid(), CompletableFutureWrapper.newInstance());
            context.writeAndFlush(s);
            log.info("client发出数据:" + s);
            ResponseInfo responseInfo = takeRpcResponse(requestInfo.getUuid());
            if (responseInfo == null) {
                return null;
            }
            return objectMapper.writeValueAsString(responseInfo.getResult());
        } catch (JsonProcessingException e) {
            throw CustomException.wrap(e.getMessage(), e);
        }
    }

    /**
     * 获取响应结果,默认超时时间30秒,超过30秒抛出AcquireResultTimeoutException
     *
     * @param requestId 请求ID
     */
    @Nullable
    public ResponseInfo takeRpcResponse(String requestId) {
        ResponseInfo responseInfo = Optional.ofNullable(queueMap.get(requestId)).map(futureWrapper -> {
            ResponseInfo res = futureWrapper.get(futureWrapper.getTimeoutSeconds() - 1, TimeUnit.SECONDS);
            futureWrapper.clear();
            return res;
        }).orElse(null);
        queueMap.remove(requestId);
        return responseInfo;
    }
}
