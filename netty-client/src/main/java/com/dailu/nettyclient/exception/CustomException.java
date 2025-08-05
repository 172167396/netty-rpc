package com.dailu.nettyclient.exception;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CustomException extends RuntimeException {
    private CustomException(String msg, Throwable e) {
        super(msg, e);
    }

    private CustomException(String msg) {
        super(msg);
    }


    public static CustomException wrap(String msg) {
        return new CustomException(msg);
    }

    public static CustomException wrap(String msg, Throwable e) {
        return new CustomException(msg, e);
    }
}
