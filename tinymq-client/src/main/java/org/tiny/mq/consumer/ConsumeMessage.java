package org.tiny.mq.consumer;


import org.tiny.mq.common.dto.ConsumeMsgCommitLogDTO;

public class ConsumeMessage {

    private int queueId;

    private ConsumeMsgCommitLogDTO consumeMsgCommitLogDTO;

    public int getQueueId() {
        return queueId;
    }

    public void setQueueId(int queueId) {
        this.queueId = queueId;
    }

    public ConsumeMsgCommitLogDTO getConsumeMsgCommitLogDTO() {
        return consumeMsgCommitLogDTO;
    }

    public void setConsumeMsgCommitLogDTO(ConsumeMsgCommitLogDTO consumeMsgCommitLogDTO) {
        this.consumeMsgCommitLogDTO = consumeMsgCommitLogDTO;
    }
}
