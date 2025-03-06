package org.tiny.mq.common.dto;


public class ConsumeMsgAckRespDTO extends BaseBrokerRemoteDTO {

    /**
     * ack是否成功
     *
     * @see org.idea.eaglemq.common.enums.AckStatus
     */
    private int ackStatus;

    public int getAckStatus() {
        return ackStatus;
    }

    public void setAckStatus(int ackStatus) {
        this.ackStatus = ackStatus;
    }
}
