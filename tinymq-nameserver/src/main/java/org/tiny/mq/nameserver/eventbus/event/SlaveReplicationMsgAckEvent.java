package org.tiny.mq.nameserver.eventbus.event;

import org.tiny.mq.common.eventbus.Event;

public class SlaveReplicationMsgAckEvent extends Event {
    @Override
    public String toString() {
        return "SlaveReplicationMsgAckEvent{} " + super.toString();
    }
}
