package org.tiny.mq.common.dto;

public class MessageRetryDTO {
    private String topic;
    private String consumeGroup;
    private int queueId;
    private int sourceCommitLogOffset;
    private long sourceCommitLogSize;
    private int commitLogName;
    private long nextRetryTime;
    private int retryCount;

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

    public int getQueueId() {
        return queueId;
    }

    public void setQueueId(int queueId) {
        this.queueId = queueId;
    }

    public int getSourceCommitLogOffset() {
        return sourceCommitLogOffset;
    }

    public long getSourceCommitLogSize() {
        return sourceCommitLogSize;
    }

    public void setSourceCommitLogSize(long sourceCommitLogSize) {
        this.sourceCommitLogSize = sourceCommitLogSize;
    }

    public void setSourceCommitLogOffset(int sourceCommitLogOffset) {
        this.sourceCommitLogOffset = sourceCommitLogOffset;
    }


    public void setSourceCommitLogSize(int sourceCommitLogSize) {
        this.sourceCommitLogSize = sourceCommitLogSize;
    }

    public int getCommitLogName() {
        return commitLogName;
    }

    public void setCommitLogName(int commitLogName) {
        this.commitLogName = commitLogName;
    }

    public long getNextRetryTime() {
        return nextRetryTime;
    }

    public void setNextRetryTime(long nextRetryTime) {
        this.nextRetryTime = nextRetryTime;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }
}
