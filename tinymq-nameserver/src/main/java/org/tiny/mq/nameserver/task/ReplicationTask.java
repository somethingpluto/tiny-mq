package org.tiny.mq.nameserver.task;

public abstract class ReplicationTask {
    private final String taskName;

    public ReplicationTask(String taskName) {
        this.taskName = taskName;
    }

    abstract void startTask();

    public void startTaskASync() {
        Thread thread = new Thread(() -> {
            startTask();
        });
        thread.setName(taskName);
        thread.start();
    }
}
