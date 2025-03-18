package org.tiny.mq.common.dto;

public class ConsumeMsgRetryRespDTO extends BaseBrokerRemoteDTO {
    private int ackStatus;

    public int getAckStatus() {
        return ackStatus;
    }

    public void setAckStatus(int ackStatus) {
        this.ackStatus = ackStatus;
    }
}
