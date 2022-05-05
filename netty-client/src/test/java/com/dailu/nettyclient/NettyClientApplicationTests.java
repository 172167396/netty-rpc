package com.dailu.nettyclient;

import com.dailu.nettyclient.utils.BlockedQueue;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class NettyClientApplicationTests {

    @Test
    void contextLoads() throws InterruptedException {
        BlockedQueue<String> blockedQueue = new BlockedQueue<>();
        Thread t1 = new Thread(() -> {
            try {
                System.out.println("t1睡眠3秒");
                Thread.sleep(3000);
                blockedQueue.add("张三");
                System.out.println("t1 add完成");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        Thread t2 = new Thread(() -> {
            System.out.println("t2开始poll......");
            System.out.println("t2获取到：" + blockedQueue.poll());
        });
        Thread t3 = new Thread(() -> {
            System.out.println("t3开始poll......");
            System.out.println("t3获取到：" + blockedQueue.poll());
        });

        Thread t4 = new Thread(() -> {
            System.out.println("t4睡眠3秒");
            blockedQueue.add("李四");
            System.out.println("t4 add完成");
        });
        t1.start();
        t2.start();
        t3.start();
        Thread.sleep(3000);
        t4.start();

        Thread.sleep(10000);
    }

}
