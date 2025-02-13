package org.tiny.mq.nameserver.eventbus.listener;

import org.tiny.mq.nameserver.eventbus.event.SlaveReplicationMsgAckEvent;

public class SlaveReplicationMsgAckListener implements Listener<SlaveReplicationMsgAckEvent> {
    @Override
    public void onReceive(SlaveReplicationMsgAckEvent event) throws IllegalAccessException {
        String msgId = event.getMsgId();
    }
}
