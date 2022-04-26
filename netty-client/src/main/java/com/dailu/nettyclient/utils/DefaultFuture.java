package com.dailu.nettyclient.utils;

import com.dailu.nettycommon.dto.ResponseInfo;

public class DefaultFuture {
    private ResponseInfo responseInfo;
    private volatile boolean isSucceed = false;
    private final Object object = new Object();

    public ResponseInfo getRpcResponse(int timeout) {
        synchronized (object) {
            while (!isSucceed) {
                try {
                    object.wait(timeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return responseInfo;
        }
    }

    public void setResponse(ResponseInfo response) {
        if (isSucceed) {
            return;
        }
        synchronized (object) {
            this.responseInfo = response;
            this.isSucceed = true;
            object.notify();
        }
    }
}