package com.dailu.nettycommon.propertiy;

/**
 * 消费者
 */
public class ClientBootStrap {
    private static final String host = "127.0.0.1";
    private static final int port = 9999;

    public static String getHost() {
        return host;
    }

    public static int getPort() {
        return port;
    }

}
