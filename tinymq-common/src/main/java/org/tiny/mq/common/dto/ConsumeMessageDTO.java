package org.tiny.mq.common.dto;

public class ConsumeMessageDTO {
    private int queueId;
    private byte[] body;

    public int getQueueId() {
        return queueId;
    }

    public void setQueueId(int queueId) {
        this.queueId = queueId;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }
}
