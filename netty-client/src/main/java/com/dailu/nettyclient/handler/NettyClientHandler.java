package com.dailu.nettyclient.handler;

import com.dailu.nettyclient.exception.AcquireResultTimeoutException;
import com.dailu.nettyclient.exception.CustomException;
import com.dailu.nettyclient.model.dto.CompletableFutureWrapper;
import com.dailu.nettyclient.utils.ApplicationContextHolder;
import com.dailu.nettycommon.dto.RequestInfo;
import com.dailu.nettycommon.dto.ResponseInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

    public CompletableFuture<String> send(RequestInfo requestInfo) {
        String requestId = requestInfo.getUuid();
        CompletableFutureWrapper<ResponseInfo> wrapper = CompletableFutureWrapper.newInstance();
        queueMap.put(requestId, wrapper);

        String s;
        try {
            s = objectMapper.writeValueAsString(requestInfo);
        } catch (JsonProcessingException e) {
            queueMap.remove(requestId);
            wrapper.clear();
            throw CustomException.wrap(e.getMessage(), e);
        }

        ChannelFuture channelFuture = context.writeAndFlush(s);
        log.info("client发出数据:{}", s);

        // 发送失败时立即清理，避免 Map 泄漏
        channelFuture.addListener((ChannelFutureListener) f -> {
            if (!f.isSuccess()) {
                queueMap.remove(requestId);
                wrapper.completeExceptionally(f.cause());
                wrapper.clear();
            }
        });

        return wrapper.getFuture()
                .thenApply(responseInfo -> {
                    try {
                        return objectMapper.writeValueAsString(responseInfo.getResult());
                    } catch (JsonProcessingException e) {
                        throw CustomException.wrap(e.getMessage(), e);
                    }
                })
                .whenComplete((result, ex) -> {
                    // 无论成功/失败/超时，保证清理 Map
                    queueMap.remove(requestId);
                    wrapper.clear();
                });
    }

    /**
     * 同步发送请求并阻塞等待响应，内部调用 {@link #send(RequestInfo)} 后 .get(timeout, unit)
     *
     * @param requestInfo 请求信息
     * @param timeout     超时时间
     * @param unit        时间单位
     * @return 响应 JSON 字符串
     */
    public String sendSync(RequestInfo requestInfo, long timeout, TimeUnit unit) {
        try {
            return send(requestInfo).get(timeout, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw CustomException.wrap("线程被中断", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof CustomException) {
                throw (CustomException) cause;
            }
            throw CustomException.wrap(cause != null ? cause.getMessage() : e.getMessage(), cause != null ? cause : e);
        } catch (TimeoutException e) {
            throw new AcquireResultTimeoutException("获取结果超时", e);
        }
    }
}
