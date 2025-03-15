package org.tiny.mq.async.spi.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.async.model.BrokerConnectionClosedEvent;
import org.tiny.mq.common.event.Listener;

public class BrokerConnectionClosedListener implements Listener<BrokerConnectionClosedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(BrokerConnectionClosedListener.class);

    @Override
    public void onReceive(BrokerConnectionClosedEvent event) throws Exception {
        String brokerReqId = event.getBrokerReqId();
        logger.info("{} closed", brokerReqId);
    }
}
