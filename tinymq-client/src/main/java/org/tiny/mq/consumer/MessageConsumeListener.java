package org.tiny.mq.consumer;


import java.util.List;

public interface MessageConsumeListener {


    /**
     * 默认的消费处理函数
     *
     * @param consumeMessages
     * @return
     */
    ConsumeResult consume(List<ConsumeMessage> consumeMessages);
}
