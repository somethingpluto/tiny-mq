package org.tiny.mq.utils;

public interface AckMessageLock {
    /**
     * 加锁
     */
    void lock();

    /**
     * 解锁
     */
    void unlock();
}
