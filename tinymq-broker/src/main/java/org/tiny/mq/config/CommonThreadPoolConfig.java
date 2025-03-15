package org.tiny.mq.config;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class CommonThreadPoolConfig {

    public static ThreadPoolExecutor refreshEagleMqTopicExecutor = new ThreadPoolExecutor(1,
            1,
            30, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10), r -> {
        Thread thread = new Thread(r);
        thread.setName("refresh-tiny-mq-topic-config");
        return thread;
    });


    public static ThreadPoolExecutor refreshConsumeQueueOffsetExecutor = new ThreadPoolExecutor(1,
            1,
            30, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10), r -> {
        Thread thread = new Thread(r);
        thread.setName("refresh-tiny-mq-topic-config");
        return thread;
    });
}
