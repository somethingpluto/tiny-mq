package org.tiny.mq.common.dto;

import java.util.List;

public class ConsumeMsgRespDTO {

    /**
     * 队列id
     */
    private Integer queueId;

    /**
     * 拉数据返回内容
     */
    private List<ConsumeMsgCommitLogDTO> commitLogContentList;


    public Integer getQueueId() {
        return queueId;
    }

    public void setQueueId(Integer queueId) {
        this.queueId = queueId;
    }

    public List<ConsumeMsgCommitLogDTO> getCommitLogContentList() {
        return commitLogContentList;
    }

    public void setCommitLogContentList(List<ConsumeMsgCommitLogDTO> commitLogContentList) {
        this.commitLogContentList = commitLogContentList;
    }
}
