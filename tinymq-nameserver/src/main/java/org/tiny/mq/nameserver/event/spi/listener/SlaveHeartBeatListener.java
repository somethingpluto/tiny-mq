package org.tiny.mq.nameserver.event.spi.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.common.event.Listener;
import org.tiny.mq.nameserver.event.model.SlaveHeartBeatEvent;


public class SlaveHeartBeatListener implements Listener<SlaveHeartBeatEvent> {

    private final Logger logger = LoggerFactory.getLogger(SlaveHeartBeatListener.class);

    @Override
    public void onReceive(SlaveHeartBeatEvent event) throws Exception {
        logger.info("接收到从节点心跳信号");
    }
}
