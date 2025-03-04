package org.tiny.mq.netty.nameserver.manager;

import org.tiny.mq.netty.nameserver.task.HeartBeatTask;

public class HeartBeatManager {

    public void startTask() {
        Thread heartBeatRequestTask = new Thread(new HeartBeatTask());
        heartBeatRequestTask.setName("heart-beat-request-task");
        heartBeatRequestTask.start();
    }
}
