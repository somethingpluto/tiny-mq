package org.tiny.mq.common.dto;

public class ConsumeMsgAckRespDTO extends BaseBrokerRemoteDTO {
    private int ackStatus;

    public int getAckStatus() {
        return ackStatus;
    }

    public void setAckStatus(int ackStatus) {
        this.ackStatus = ackStatus;
    }

    @Override
    public String toString() {
        return "ConsumeMsgAckRespDTO{" +
                "ackStatus=" + ackStatus +
                "} " + super.toString();
    }
}
