package org.tiny.mq.common.dto;

import java.util.List;

public class ConsumeMsgRespDTO {
    private Integer queueId;
    private List<byte[]> commitLogContentList;

    public Integer getQueueId() {
        return queueId;
    }

    public void setQueueId(Integer queueId) {
        this.queueId = queueId;
    }

    public List<byte[]> getCommitLogContentList() {
        return commitLogContentList;
    }

    public void setCommitLogContentList(List<byte[]> commitLogContentList) {
        this.commitLogContentList = commitLogContentList;
    }
}
