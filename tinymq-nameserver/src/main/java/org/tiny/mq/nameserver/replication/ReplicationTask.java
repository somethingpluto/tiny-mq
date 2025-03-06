package org.tiny.mq.nameserver.replication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ReplicationTask {

    private final Logger logger = LoggerFactory.getLogger(ReplicationTask.class);

    private String taskName;

    public ReplicationTask(String taskName) {
        this.taskName = taskName;
    }

    public void startTaskAsync() {
        Thread task = new Thread(() -> {
            logger.info("start job:" + taskName);
            startTask();
        });
        task.setName(taskName);
        task.start();
    }

    abstract void startTask();
}
