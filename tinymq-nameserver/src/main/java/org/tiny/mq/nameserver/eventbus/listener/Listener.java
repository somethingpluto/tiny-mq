package org.tiny.mq.nameserver.eventbus.listener;

import org.tiny.mq.nameserver.eventbus.event.Event;

public interface Listener<E extends Event> {
    /**
     * 回调通知
     *
     * @param event
     */
    void onReceive(E event) throws IllegalAccessException;
}
