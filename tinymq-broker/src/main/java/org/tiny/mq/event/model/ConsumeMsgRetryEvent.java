package org.tiny.mq.event.model;

import org.tiny.mq.common.dto.ConsumeMsgRetryReqDTO;
import org.tiny.mq.common.event.model.Event;

public class ConsumeMsgRetryEvent extends Event {
    private ConsumeMsgRetryReqDTO consumeMsgLaterReqDTO;

    public ConsumeMsgRetryReqDTO getConsumeMsgLaterReqDTO() {
        return consumeMsgLaterReqDTO;
    }

    public void setConsumeMsgLaterReqDTO(ConsumeMsgRetryReqDTO consumeMsgLaterReqDTO) {
        this.consumeMsgLaterReqDTO = consumeMsgLaterReqDTO;
    }
}
