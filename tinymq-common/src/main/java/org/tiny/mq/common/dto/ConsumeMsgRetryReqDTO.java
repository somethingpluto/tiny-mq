package org.tiny.mq.common.dto;

import java.util.List;

public class ConsumeMsgRetryReqDTO extends BaseBrokerRemoteDTO {
    private String topic;
    private String consumeGroup;
    private Integer queueId;
    private String ip;
    private Integer port;
    private Integer ackCount;
    private List<Long> commitLogOffsetList;
    private List<Integer> consumeQueueOffsetList;
    private int retryTime;

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

    public Integer getAckCount() {
        return ackCount;
    }

    public void setAckCount(Integer ackCount) {
        this.ackCount = ackCount;
    }

    public List<Long> getCommitLogOffsetList() {
        return commitLogOffsetList;
    }

    public void setCommitLogOffsetList(List<Long> commitLogOffsetList) {
        this.commitLogOffsetList = commitLogOffsetList;
    }

    public List<Integer> getConsumeQueueOffsetList() {
        return consumeQueueOffsetList;
    }

    public void setConsumeQueueOffsetList(List<Integer> consumeQueueOffsetList) {
        this.consumeQueueOffsetList = consumeQueueOffsetList;
    }

    public int getRetryTime() {
        return retryTime;
    }

    public void setRetryTime(int retryTime) {
        this.retryTime = retryTime;
    }

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
