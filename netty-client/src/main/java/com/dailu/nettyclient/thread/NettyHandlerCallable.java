package com.dailu.nettyclient.thread;

import com.dailu.nettyclient.handler.NettyClientHandler;
import com.dailu.nettycommon.dto.RequestInfo;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.Callable;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@RequiredArgsConstructor
public class NettyHandlerCallable implements Callable<String> {

    private final NettyClientHandler nettyClientHandler;

    private final RequestInfo requestInfo;

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    @Override
    public String call() throws Exception {
        return null;
    }
}
