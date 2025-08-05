package com.dailu.nettyclient.utils;

import com.dailu.nettyclient.exception.AcquireResultTimeoutException;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 简单的阻塞队列
 * 默认超时时间为10分钟,超过10分钟没有poll,自动从map删除
 */
@Slf4j
public class BlockedQueue<T> implements CustomBlockQueue<T> {
    private final List<T> data = new ArrayList<>(1);
    private final LocalDateTime createTime = LocalDateTime.now();
    private final int expireMinute;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();


    public BlockedQueue() {
        this(10);
    }

    public BlockedQueue(int expireMinute) {
        if (expireMinute < 0 || expireMinute > 120) {
            throw new IllegalArgumentException("expireMinute must between 0 and 120");
        }
        this.expireMinute = expireMinute;
    }

    public boolean add(T t) {
        lock.lock();
        try {
            data.add(t);
            condition.signal();
            return true;
        } finally {
            lock.unlock();
        }
    }

    public T poll() {
        return poll(30);
    }

    public T poll(long seconds) {
        lock.lock();
        seconds = seconds <= 0 ? 30 : seconds;
        try {
            while (data.isEmpty()) {
                boolean acquiredBeforeExpire = condition.await(seconds, TimeUnit.SECONDS);
                if (!acquiredBeforeExpire) {
                    throw new AcquireResultTimeoutException("获取结果超时,当前超时时间为" + seconds + "秒");
                }
            }
            return data.remove(0);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error(e.getMessage(), e);
            throw new IllegalStateException("获取结果失败");
        } finally {
            lock.unlock();
        }
    }


    public boolean isExpired() {
        return LocalDateTime.now().isAfter(createTime.plusMinutes(expireMinute));
    }
}
