package org.tiny.mq.eventbus.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.common.dto.ConsumeMsgReqDTO;
import org.tiny.mq.common.eventbus.Listener;
import org.tiny.mq.eventbus.event.ConsumeMessageEvent;

public class ConsumeMessageListener implements Listener<ConsumeMessageEvent> {
    private static final Logger logger = LoggerFactory.getLogger(ConsumeMessageListener.class);

    @Override
    public void onReceive(ConsumeMessageEvent event) throws IllegalAccessException {
        logger.info("[EVENT][Consume Message]:{}", event);
        ConsumeMsgReqDTO consumeMsgReqDTO = event.getConsumeMsgReqDTO();

    }
}
