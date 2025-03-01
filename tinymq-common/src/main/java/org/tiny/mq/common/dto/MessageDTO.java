package org.tiny.mq.common.dto;

import java.util.Arrays;

public class MessageDTO {
    private String topic;
    private int queueId = -1;
    private String msgId;
    private int sendWay;
    private byte[] body;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getQueueId() {
        return queueId;
    }

    public void setQueueId(int queueId) {
        this.queueId = queueId;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public int getSendWay() {
        return sendWay;
    }

    public void setSendWay(int sendWay) {
        this.sendWay = sendWay;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "MessageDTO{" +
                "topic='" + topic + '\'' +
                ", queueId=" + queueId +
                ", msgId='" + msgId + '\'' +
                ", sendWay=" + sendWay +
                ", body=" + Arrays.toString(body) +
                '}';
    }
}
