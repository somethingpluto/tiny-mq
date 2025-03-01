package org.tiny.mq.eventbus.listener;

import org.tiny.mq.common.dto.ConsumeMsgReqDTO;
import org.tiny.mq.common.eventbus.Listener;
import org.tiny.mq.eventbus.event.ConsumeMessageEvent;

public class ConsumeMessageListener implements Listener<ConsumeMessageEvent> {
    @Override
    public void onReceive(ConsumeMessageEvent event) throws IllegalAccessException {
        ConsumeMsgReqDTO consumeMsgReqDTO = event.getConsumeMsgReqDTO();
        
    }
}
