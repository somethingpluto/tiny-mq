package org.tiny.mq.consumer;

import org.tiny.mq.common.dto.ConsumeMessageDTO;

import java.util.List;

public interface MessageConsumeListener {
    ConsumeResult consume(List<ConsumeMessageDTO> consumeMessageList);
}
