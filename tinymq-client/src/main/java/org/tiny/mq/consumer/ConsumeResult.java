package org.tiny.mq.consumer;

import org.tiny.mq.common.enums.ConsumeResultStatus;

public class ConsumeResult {
    private int consumeResultStatus;

    public ConsumeResult(int consumeResultStatus) {
        this.consumeResultStatus = consumeResultStatus;
    }

    public int getConsumeResultStatus() {
        return consumeResultStatus;
    }

    public void setConsumeResultStatus(int consumeResultStatus) {
        this.consumeResultStatus = consumeResultStatus;
    }

    public static ConsumeResult CONSUME_SUCCESS() {
        return new ConsumeResult(ConsumeResultStatus.CONSUME_SUCCESS.getCode());
    }

    public static ConsumeResult CONSUME_LATER() {
        return new ConsumeResult(ConsumeResultStatus.CONSUME_LATER.getCode());
    }
}
