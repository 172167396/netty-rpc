package com.dailu.nettyclient.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RequestFactory {
 
    private static final Map<String, Object> map = new ConcurrentHashMap<String, Object>();
 
    public static void put(String uuid, Object object) {
        map.put(uuid, object);
    }
 
    public static Object get(String uuid) {
        return map.get(uuid);
    }
}