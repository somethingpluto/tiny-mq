package org.tiny.mq.common.eventbus;


public interface Listener<E extends Event> {
    /**
     * 回调通知
     *
     * @param event
     */
    void onReceive(E event) throws IllegalAccessException;
}
