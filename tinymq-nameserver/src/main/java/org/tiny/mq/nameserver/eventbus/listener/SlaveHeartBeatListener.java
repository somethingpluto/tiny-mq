package org.tiny.mq.nameserver.eventbus.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.common.eventbus.Listener;
import org.tiny.mq.nameserver.eventbus.event.SlaveHeartBeatEvent;

public class SlaveHeartBeatListener implements Listener<SlaveHeartBeatEvent> {
    private static final Logger logger = LoggerFactory.getLogger(SlaveHeartBeatEvent.class);

    @Override
    public void onReceive(SlaveHeartBeatEvent event) {
        logger.info("[EVENT][Slave Heart Beat]:{}", event);
    }
}
