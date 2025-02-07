package org.tiny.mq.netty.nameserver.manager;

import org.tiny.mq.netty.nameserver.task.HeartBeatTask;

import java.util.concurrent.atomic.AtomicInteger;

public class HeartBeatManager {
    private final AtomicInteger flag = new AtomicInteger(0);

    public void startTask() {
        if (flag.getAndIncrement() >= 1) {
            return;
        }
        Thread heartBeatRequestTask = new Thread(new HeartBeatTask());
        heartBeatRequestTask.setName("heart-beat-request-task");
        heartBeatRequestTask.start();
    }
}
