package com.dailu.nettyclient.exception;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CustomerException extends RuntimeException {
    private CustomerException(String msg, Throwable e) {
        super(msg, e);
    }

    private CustomerException(String msg) {
        super(msg);
    }


    public static CustomerException wrap(String msg) {
        return new CustomerException(msg);
    }

    public static CustomerException wrap(String msg, Throwable e) {
        return new CustomerException(msg, e);
    }
}
