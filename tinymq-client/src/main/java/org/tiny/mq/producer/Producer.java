package org.tiny.mq.producer;


import org.tiny.mq.common.dto.MessageDTO;

public interface Producer {

    /**
     * 同步发送
     *
     * @param messageDTO
     * @return
     */
    SendResult send(MessageDTO messageDTO);

    /**
     * 异步发送
     *
     * @param messageDTO
     * @return
     */
    void sendAsync(MessageDTO messageDTO);
}
