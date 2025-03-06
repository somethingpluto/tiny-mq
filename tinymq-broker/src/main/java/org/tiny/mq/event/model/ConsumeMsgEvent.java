package org.tiny.mq.event.model;


import org.tiny.mq.common.dto.ConsumeMsgReqDTO;
import org.tiny.mq.common.event.model.Event;

public class ConsumeMsgEvent extends Event {

    private ConsumeMsgReqDTO consumeMsgReqDTO;

    public ConsumeMsgReqDTO getConsumeMsgReqDTO() {
        return consumeMsgReqDTO;
    }

    public void setConsumeMsgReqDTO(ConsumeMsgReqDTO consumeMsgReqDTO) {
        this.consumeMsgReqDTO = consumeMsgReqDTO;
    }
}
