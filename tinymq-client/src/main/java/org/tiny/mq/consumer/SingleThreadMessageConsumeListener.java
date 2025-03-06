package org.tiny.mq.consumer;

import java.util.List;


public class SingleThreadMessageConsumeListener implements MessageConsumeListener {

    @Override
    public ConsumeResult consume(List<ConsumeMessage> consumeMessages) {
        return null;
    }
}
