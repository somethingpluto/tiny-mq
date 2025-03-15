package org.tiny.mq.common.dto;

public class ConsumeMsgRetryReqDTO extends BaseBrokerRemoteDTO {
    private String topic;
    private String consumeGroup;
    private Integer queueId;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getConsumeGroup() {
        return consumeGroup;
    }

    public void setConsumeGroup(String consumeGroup) {
        this.consumeGroup = consumeGroup;
    }

    public Integer getQueueId() {
        return queueId;
    }

    public void setQueueId(Integer queueId) {
        this.queueId = queueId;
    }
}
