package org.tiny.mq.event.model;


import org.tiny.mq.common.dto.MessageDTO;
import org.tiny.mq.common.event.model.Event;

public class PushMsgEvent extends Event {

    private MessageDTO messageDTO;

    public MessageDTO getMessageDTO() {
        return messageDTO;
    }

    public void setMessageDTO(MessageDTO messageDTO) {
        this.messageDTO = messageDTO;
    }
}
