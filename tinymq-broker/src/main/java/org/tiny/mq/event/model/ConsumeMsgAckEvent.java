package org.tiny.mq.event.model;

import org.tiny.mq.common.dto.ConsumeMsgAckReqDTO;
import org.tiny.mq.common.event.model.Event;

public class ConsumeMsgAckEvent extends Event {
    private ConsumeMsgAckReqDTO consumeMsgAckReqDTO;

    public ConsumeMsgAckReqDTO getConsumeMsgAckReqDTO() {
        return consumeMsgAckReqDTO;
    }

    public void setConsumeMsgAckReqDTO(ConsumeMsgAckReqDTO consumeMsgAckReqDTO) {
        this.consumeMsgAckReqDTO = consumeMsgAckReqDTO;
    }
}
