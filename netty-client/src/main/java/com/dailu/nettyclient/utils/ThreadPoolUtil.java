package com.dailu.nettyclient.utils;


import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public final class ThreadPoolUtil {
    public static final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(3, 5, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<>(10));
}
