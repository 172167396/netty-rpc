package com.dailu.nettyclient.utils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BlockedQueue<T> {
    private final List<T> data = new ArrayList<>(1);
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    public void add(T t) {
        lock.lock();
        try {
            data.add(t);
            condition.signal();
        } finally {
            lock.unlock();
        }
    }

    public T poll() {
        lock.lock();
        try {
            while (data.isEmpty()) {
                condition.await();
            }
            return data.remove(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return null;
    }

}
