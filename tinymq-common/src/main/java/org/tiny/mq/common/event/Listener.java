package org.tiny.mq.common.event;


import org.tiny.mq.common.event.model.Event;


public interface Listener<E extends Event> {

    /**
     * 回调通知
     *
     * @param event
     */
    void onReceive(E event) throws Exception;
}
