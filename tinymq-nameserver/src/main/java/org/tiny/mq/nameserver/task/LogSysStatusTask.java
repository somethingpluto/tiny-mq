package org.tiny.mq.nameserver.task;

import org.tiny.mq.nameserver.config.SysStatusLogger;

import java.util.concurrent.TimeUnit;

public class LogSysStatusTask implements Runnable {

    @Override
    public void run() {
        while (true) {
            try {
                TimeUnit.SECONDS.sleep(2);
                SysStatusLogger.logSysStatus();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        new Thread(new LogSysStatusTask(), "sys status").start();
    }
}
