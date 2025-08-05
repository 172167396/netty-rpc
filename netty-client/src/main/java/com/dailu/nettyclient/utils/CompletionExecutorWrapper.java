package com.dailu.nettyclient.utils;

import com.dailu.nettyclient.exception.AcquireResultTimeoutException;
import com.dailu.nettyclient.exception.CustomException;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

@Slf4j
@SuppressWarnings({"unused", "UnusedReturnValue"})
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CompletionExecutorWrapper<T> {

    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    private CompletionService<T> completionService;

    private final ReentrantLock lock = new ReentrantLock();

    private final Condition condition = lock.newCondition();

    private final AtomicBoolean finished = new AtomicBoolean();
    private final AtomicInteger counter = new AtomicInteger(0);


    public static <T> CompletionExecutorWrapper<T> of(ThreadPoolTaskExecutor threadPoolTaskExecutor) {
        CompletionExecutorWrapper<T> completionExecutorWrapper = new CompletionExecutorWrapper<>();
        completionExecutorWrapper.completionService = new ExecutorCompletionService<>(threadPoolTaskExecutor);
        return completionExecutorWrapper;
    }


    public List<ResultWrapper<T>> gatherAll() {
        return gatherAll(null);
    }

    public List<ResultWrapper<T>> gatherAll(Consumer<ResultWrapper<T>> handler) {
        List<ResultWrapper<T>> result = new ArrayList<>();
        int submitCount = counter.get();
        for (int i = 0; i < submitCount; i++) {
            ResultWrapper<T> resultWrapper = new ResultWrapper<>();
            try {
                resultWrapper.result = completionService.take().get();
                resultWrapper.success = true;
                result.add(resultWrapper);
            } catch (InterruptedException | ExecutionException e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                resultWrapper.success = false;
                resultWrapper.throwable = e;
            }
            if (handler != null) {
                try {
                    handler.accept(resultWrapper);
                } catch (Exception e) {
                    log.error("调用handler处理结果出错:{}", e.getMessage(), e);
                }
            }
        }
        onFinished();
        return result;
    }


    /**
     * 获取当前所有任务是否都已执行完
     */
    public boolean isFinished() {
        lock.lock();
        try {
            return finished.get();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取当前所有任务是否都已执行完
     * 等待超时会抛出{@link AcquireResultTimeoutException}
     *
     * @param awaitTime 获取结果等待时长
     * @param timeUnit  时长单位
     */
    public boolean isFinished(long awaitTime, TimeUnit timeUnit) {
        lock.lock();
        try {
            while (!finished.get()) {
                boolean acquiredBeforeExpire = condition.await(awaitTime, timeUnit);
                if (acquiredBeforeExpire) {
                    throw new AcquireResultTimeoutException("获取结果超时，当前超时时间为" + awaitTime + " " + timeUnit.toString());
                }
            }
            return finished.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw CustomException.wrap(e.getMessage(), e);
        } finally {
            lock.unlock();
        }
    }

    private void onFinished() {
        lock.lock();
        try {
            finished.set(true);
            condition.signal();
        } finally {
            lock.unlock();
        }
    }



    /**
     * 阻塞方式获取任务是否完成
     */
    public boolean waitFinished() {
        lock.lock();
        try {
            while (!finished.get()) {
                condition.await();
            }
            return finished.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw CustomException.wrap(e.getMessage(), e);
        } finally {
            lock.unlock();
        }
    }


    public Future<T> submit(Callable<T> task) {
        counter.incrementAndGet();
        return completionService.submit(task);
    }

    public Future<T> submit(Runnable task, T result) {
        counter.incrementAndGet();
        return completionService.submit(task, result);
    }

    public Future<T> take() throws InterruptedException {
        return completionService.take();
    }

    public Future<T> poll() {
        return completionService.poll();
    }

    public Future<T> poll(long timeout, TimeUnit unit) throws InterruptedException {
        return completionService.poll(timeout, unit);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResultWrapper<T> {
        private T result;
        private boolean success;
        private Throwable throwable;
    }

}
