package org.tiny.mq.eventbus.event;

import org.tiny.mq.common.dto.MessageDTO;
import org.tiny.mq.common.eventbus.Event;

public class PushMessageEvent extends Event {
    private MessageDTO messageDTO;

    public MessageDTO getMessageDTO() {
        return messageDTO;
    }

    public void setMessageDTO(MessageDTO messageDTO) {
        this.messageDTO = messageDTO;
    }
}
