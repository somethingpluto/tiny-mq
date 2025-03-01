package org.tiny.mq.producer;

import org.tiny.mq.common.dto.MessageDTO;

public interface Producer {
    /**
     * 同步发送
     *
     * @param message
     * @return
     */
    SendResult sendMessage(MessageDTO message);

    /**
     * 异步发送
     *
     * @param message
     * @return
     */
    void sendAsyncMessage(MessageDTO message);
}
