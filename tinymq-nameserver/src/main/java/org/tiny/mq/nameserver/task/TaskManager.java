package org.tiny.mq.nameserver.task;

public class TaskManager {
    public static void startInvalidServiceRemoveTask() {
        Thread inValidServiceRemoveTask = new Thread(new ServiceInstanceRemoveTask());
        inValidServiceRemoveTask.setName("invalid-server-remove-task");
        inValidServiceRemoveTask.start();
    }
}
