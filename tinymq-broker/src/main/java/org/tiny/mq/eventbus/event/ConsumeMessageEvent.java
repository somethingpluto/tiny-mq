package org.tiny.mq.eventbus.event;

import org.tiny.mq.common.dto.ConsumeMsgReqDTO;
import org.tiny.mq.common.eventbus.Event;

public class ConsumeMessageEvent extends Event {
    private ConsumeMsgReqDTO consumeMsgReqDTO;

    public ConsumeMsgReqDTO getConsumeMsgReqDTO() {
        return consumeMsgReqDTO;
    }

    public void setConsumeMsgReqDTO(ConsumeMsgReqDTO consumeMsgReqDTO) {
        this.consumeMsgReqDTO = consumeMsgReqDTO;
    }

    @Override
    public String toString() {
        return "ConsumeMessageEvent{" +
                "consumeMsgReqDTO=" + consumeMsgReqDTO +
                "} " + super.toString();
    }
}
