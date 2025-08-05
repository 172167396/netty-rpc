package com.dailu.nettyclient.utils;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class R<T> {

    private static final int SUCCESS_CODE = 0;
    private static final String SUCCESS_MSG = "操作成功";
    private static final int FAIL_CODE = -1;
    private static final String FAIL_MSG = "操作失败";

    private T data;

    private int code;

    private String msg;


    public static <T> R<T> success() {
        return new R<>(null, SUCCESS_CODE, SUCCESS_MSG);
    }
    public static <T> R<T> success(T data) {
        return new R<>(data, SUCCESS_CODE, SUCCESS_MSG);
    }

    public static <T> R<T> fail() {
        return new R<>(null, FAIL_CODE, FAIL_MSG);
    }

    public static <T> R<T> fail(String msg) {
        return new R<>(null, FAIL_CODE, msg);
    }

}
