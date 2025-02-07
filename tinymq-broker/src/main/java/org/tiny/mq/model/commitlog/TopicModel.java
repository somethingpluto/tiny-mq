package org.tiny.mq.model.commitlog;

import org.tiny.mq.model.commitlog.CommitLogModel;
import org.tiny.mq.model.consumequeue.QueueModel;

import java.util.List;

public class TopicModel {
    /**
     * 主题名称
     */
    private String topicName;
    /**
     * 主题日志文件信息
     */
    private CommitLogModel commitLogModel;
    /**
     * 队列列表
     */
    private List<QueueModel> queueList;
    /**
     * 创建时间
     */
    public Long createAt;
    /**
     * 更新时间
     */
    public Long updateAt;

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public CommitLogModel getCommitLogModel() {
        return commitLogModel;
    }

    public void setCommitLogModel(CommitLogModel commitLogModel) {
        this.commitLogModel = commitLogModel;
    }

    public List<QueueModel> getQueueList() {
        return queueList;
    }

    public void setQueueList(List<QueueModel> queueList) {
        this.queueList = queueList;
    }

    public Long getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Long createAt) {
        this.createAt = createAt;
    }

    public Long getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(Long updateAt) {
        this.updateAt = updateAt;
    }

    @Override
    public String toString() {
        return "TopicModel{" +
                "topicName='" + topicName + '\'' +
                ", commitLogModel=" + commitLogModel +
                ", queueList=" + queueList +
                ", createAt=" + createAt +
                ", updateAt=" + updateAt +
                '}';
    }
}
