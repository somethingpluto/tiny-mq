package org.tiny.mq.common.dto;

public class ConsumeMsgAckReqDTO extends BaseBrokerRemoteDTO {
    private String topic;
    private String consumerGroup;
    private Integer queueId;
    private Integer ackCount;
    private String ip;
    private Integer port;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public Integer getQueueId() {
        return queueId;
    }

    public void setQueueId(Integer queueId) {
        this.queueId = queueId;
    }

    public Integer getAckCount() {
        return ackCount;
    }

    public void setAckCount(Integer ackCount) {
        this.ackCount = ackCount;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "ConsumeMsgAckReqDTO{" +
                "topic='" + topic + '\'' +
                ", consumerGroup='" + consumerGroup + '\'' +
                ", queueId=" + queueId +
                ", ackCount=" + ackCount +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                "} " + super.toString();
    }
}
