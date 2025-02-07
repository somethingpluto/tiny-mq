package org.tiny.mq.core.consumequeue;

import org.tiny.mq.config.GlobalCache;
import org.tiny.mq.model.consumequeue.QueueModel;
import org.tiny.mq.model.commitlog.TopicModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConsumeQueueAppendHandler {

    /**
     * 初始化主题下的所有队列MMap
     * @param topicName 主题名称
     */
    public void prepareConsumeQueue(String topicName) throws IOException {
        TopicModel topicModel = GlobalCache.getMQTopicModelMap().get(topicName);
        // 获取topic下的所有队列信息
        List<QueueModel> queueList = topicModel.getQueueList();
        List<ConsumeQueueMemoryMapFileModel> consumeQueueItemModels = new ArrayList<>();
        for (QueueModel queueModel:queueList){
            ConsumeQueueMemoryMapFileModel consumeQueueMemoryMapFileModel = new ConsumeQueueMemoryMapFileModel();
            consumeQueueMemoryMapFileModel.loadFileInMemory(
                    topicName,queueModel.getId(),
                    queueModel.getLastOffset(),
                    queueModel.getLatestOffset().get(),
                    queueModel.getOffsetLimit());
            consumeQueueItemModels.add(consumeQueueMemoryMapFileModel);
        }
        GlobalCache.getConsumeQueueMMapFileModelManager().put(topicName,consumeQueueItemModels);

    }
}
