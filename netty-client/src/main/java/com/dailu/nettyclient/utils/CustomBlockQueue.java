package com.dailu.nettyclient.utils;

public interface CustomBlockQueue<T> {

    boolean add(T t);

    T poll();

    T poll(long seconds);
}
