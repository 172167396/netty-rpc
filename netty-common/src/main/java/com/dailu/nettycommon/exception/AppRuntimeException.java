package com.dailu.nettycommon.exception;

public class AppRuntimeException extends RuntimeException {

    public AppRuntimeException() {
        super();
    }

    public AppRuntimeException(String message) {
        super(message);
    }

    public AppRuntimeException(String message, Throwable e) {
        super(message, e);
    }

    public AppRuntimeException(Throwable e) {
        super(e);
    }
}
