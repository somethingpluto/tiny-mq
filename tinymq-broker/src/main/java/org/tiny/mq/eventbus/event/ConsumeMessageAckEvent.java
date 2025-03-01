package org.tiny.mq.eventbus.event;

import org.tiny.mq.common.dto.ConsumeMsgAckReqDTO;
import org.tiny.mq.common.eventbus.Event;

public class ConsumeMessageAckEvent extends Event {
    private ConsumeMsgAckReqDTO consumeMsgReqDTO;

    public ConsumeMsgAckReqDTO getConsumeMsgReqDTO() {
        return consumeMsgReqDTO;
    }

    public void setConsumeMsgReqDTO(ConsumeMsgAckReqDTO consumeMsgReqDTO) {
        this.consumeMsgReqDTO = consumeMsgReqDTO;
    }

    @Override
    public String toString() {
        return "ConsumeMessageAckEvent{" +
                "consumeMsgReqDTO=" + consumeMsgReqDTO +
                "} " + super.toString();
    }
}
