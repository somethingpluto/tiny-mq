package org.tiny.mq.event.model;

import org.tiny.mq.common.dto.CreateTopicReqDTO;
import org.tiny.mq.common.event.model.Event;

public class CreateTopicEvent extends Event {
    private CreateTopicReqDTO createTopicReqDTO;

    public CreateTopicReqDTO getCreateTopicReqDTO() {
        return createTopicReqDTO;
    }

    public void setCreateTopicReqDTO(CreateTopicReqDTO createTopicReqDTO) {
        this.createTopicReqDTO = createTopicReqDTO;
    }
}
