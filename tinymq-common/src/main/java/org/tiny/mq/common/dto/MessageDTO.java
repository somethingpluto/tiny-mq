package org.tiny.mq.common.dto;

public class MessageDTO {

    private String topic;
    private int queueId = -1;
    private String msgId;
    private int sendWay;
    private byte[] body;
    private boolean isRetry;
    private int delay;

    private int txFlag = -1;

    private int localTxState = -1;

    public int getLocalTxState() {
        return localTxState;
    }

    public void setLocalTxState(int localTxState) {
        this.localTxState = localTxState;
    }

    public int getTxFlag() {
        return txFlag;
    }

    public void setTxFlag(int txFlag) {
        this.txFlag = txFlag;
    }

    public boolean isRetry() {
        return isRetry;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public void setRetry(boolean retry) {
        isRetry = retry;
    }

    public int getSendWay() {
        return sendWay;
    }

    public void setSendWay(int sendWay) {
        this.sendWay = sendWay;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

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

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }
}
