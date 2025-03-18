package org.tiny.mq.event.model;

import org.tiny.mq.common.dto.ConsumeMsgRetryReqDTO;
import org.tiny.mq.common.event.model.Event;

public class ConsumeMsgRetryEvent extends Event {
    private ConsumeMsgRetryReqDTO consumeMsgRetryReqDTO;

    public ConsumeMsgRetryReqDTO getConsumeMsgRetryReqDTO() {
        return consumeMsgRetryReqDTO;
    }

    public void setConsumeMsgRetryReqDTO(ConsumeMsgRetryReqDTO consumeMsgRetryReqDTO) {
        this.consumeMsgRetryReqDTO = consumeMsgRetryReqDTO;
    }
}
