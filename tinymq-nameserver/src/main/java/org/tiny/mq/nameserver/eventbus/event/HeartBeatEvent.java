package org.tiny.mq.nameserver.eventbus.event;

import org.tiny.mq.common.eventbus.Event;

public class HeartBeatEvent extends Event {
    @Override
    public String toString() {
        return "HeartBeatEvent{} " + super.toString();
    }
}
