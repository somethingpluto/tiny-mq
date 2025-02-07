package org.tiny.mq.model;

import org.tiny.mq.config.GlobalCache;
import org.tiny.mq.model.commitlog.TopicModel;
import org.tiny.mq.model.consumequeue.ConsumeQueueOffsetModel;

import java.util.Map;

public class ModelManager {
    public static TopicModel getTopicModel(String topicName){
        TopicModel topicModel = GlobalCache.getMQTopicModelMap().get(topicName);
        if (topicModel == null){
            throw new IllegalArgumentException("topic name is invalid");
        }
        return topicModel;
    }

    public static ConsumeQueueOffsetModel.ConsumeGroupInfo getTopicConsumerGroup(String topicName){
        Map<String, ConsumeQueueOffsetModel.ConsumeGroupInfo> consumeGroupInfoMap = GlobalCache.getConsumeQueueOffsetModel().getOffsetTable();
        ConsumeQueueOffsetModel.ConsumeGroupInfo consumeGroupInfo = consumeGroupInfoMap.get(topicName);
        if (consumeGroupInfo == null){
            throw new IllegalArgumentException("topic [{}] not have no consumeGroup");
        }
        return consumeGroupInfo;
    }
}
