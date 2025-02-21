package org.tiny.mq.nameserver.replication;

public abstract class ReplicationTask {

    private final String taskName;

    public ReplicationTask(String taskName) {
        this.taskName = taskName;
    }

    public void startTaskAsync() {
        Thread task = new Thread(() -> {
            System.out.println("start job:" + taskName);
            startTask();
        });
        task.setName(taskName);
        task.start();
    }

    abstract void startTask();
}
