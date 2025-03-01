package org.tiny.mq.common.dto;

public class SendMessageToBrokerRespDTO extends BaseBrokerRemoteDTO {
    private int status;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "SendMessageToBrokerRespDTO{" +
                "status=" + status +
                "} " + super.toString();
    }
}
