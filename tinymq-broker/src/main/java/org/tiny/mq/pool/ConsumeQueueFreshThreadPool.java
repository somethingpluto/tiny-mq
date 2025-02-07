package org.tiny.mq.pool;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ConsumeQueueFreshThreadPool {
    public static ThreadPoolExecutor refreshConsumeQueueOffsetExecutor = new ThreadPoolExecutor(1,1,30, TimeUnit.SECONDS,new ArrayBlockingQueue<>(10),r -> {
        Thread thread = new Thread(r);
        thread.setName("refresh-mq-topic-config");
        return thread;
    });
}
