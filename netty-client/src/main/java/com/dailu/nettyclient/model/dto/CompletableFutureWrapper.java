package com.dailu.nettyclient.model.dto;

import com.dailu.nettyclient.exception.AcquireResultTimeoutException;
import com.dailu.nettyclient.exception.CustomException;
import lombok.*;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CompletableFutureWrapper<T> {

    private CompletableFuture<T> future;

    private ExpireAt expireAt;

    public boolean isDone() {
        return future.isDone();
    }

    @SneakyThrows
    public T get() {
        return future.get();
    }

    public T get(long timeout, TimeUnit unit) {
        try {
            return future.get(timeout, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw CustomException.wrap("线程被中断", e);
        } catch (ExecutionException e) {
            throw CustomException.wrap("执行异常", e);
        } catch (TimeoutException e) {
            throw new AcquireResultTimeoutException("获取结果超时", e);
        }
    }

    public T getNow(T valueIfAbsent) {
        return future.getNow(valueIfAbsent);
    }

    public boolean complete(T value) {
        return future.complete(value);
    }

    public boolean completeExceptionally(Throwable ex) {
        return future.completeExceptionally(ex);
    }

    public boolean isExpired() {
        return expireAt.isExpired();
    }

    public void clear() {
        this.future = null;
        this.expireAt = null;
    }

    public static <T> CompletableFutureWrapper<T> newInstance() {
        return new CompletableFutureWrapper<>(new CompletableFuture<>(), ExpireAt.defaultExpire());
    }

    public static <T> CompletableFutureWrapper<T> newInstance(ExpireAt expireAt) {
        return new CompletableFutureWrapper<>(new CompletableFuture<>(), expireAt);
    }

    public long getTimeoutSeconds() {
        return expireAt.getTimeout(ChronoUnit.SECONDS);
    }

}
