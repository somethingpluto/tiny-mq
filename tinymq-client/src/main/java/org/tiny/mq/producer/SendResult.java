package org.tiny.mq.producer;

public class SendResult {

    private SendStatus sendStatus;

    public SendStatus getSendStatus() {
        return sendStatus;
    }

    public void setSendStatus(SendStatus sendStatus) {
        this.sendStatus = sendStatus;
    }
}
