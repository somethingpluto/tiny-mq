package org.tiny.mq.common.dto;

import java.util.List;

public class ConsumeMsgRetryReqDTO extends BaseBrokerRemoteDTO {
    private String topic;
    private String consumerGroup;
    private Integer queueId;
    private String ip;
    private Integer port;
    private Integer ackCount;
    private List<Long> commitLogOffsetList;
    private List<Integer> commitLogMsgLengthList;
    private String commitLogName;
    //重试次数
    private int retryTime;

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

    public List<Integer> getCommitLogMsgLengthList() {
        return commitLogMsgLengthList;
    }

    public void setCommitLogMsgLengthList(List<Integer> commitLogMsgLengthList) {
        this.commitLogMsgLengthList = commitLogMsgLengthList;
    }

    public String getCommitLogName() {
        return commitLogName;
    }

    public void setCommitLogName(String commitLogName) {
        this.commitLogName = commitLogName;
    }

    public int getRetryTime() {
        return retryTime;
    }

    public void setRetryTime(int retryTime) {
        this.retryTime = retryTime;
    }
}
