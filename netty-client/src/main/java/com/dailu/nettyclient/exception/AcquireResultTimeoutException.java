package com.dailu.nettyclient.exception;

public class AcquireResultTimeoutException extends RuntimeException {
    public AcquireResultTimeoutException(String msg) {
        super(msg);
    }

    public AcquireResultTimeoutException(String msg, Throwable e) {
        super(msg, e);
    }
}
