package org.tiny.mq.common.dto;

/**
 * 创建Topic
 */
public class CreateTopicReqDTO extends BaseBrokerRemoteDTO {
    private String topicName;
    private Integer queueSize;

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public Integer getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(Integer queueSize) {
        this.queueSize = queueSize;
    }
}
