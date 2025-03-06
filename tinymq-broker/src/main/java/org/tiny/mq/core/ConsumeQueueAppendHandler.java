package org.tiny.mq.core;


import org.tiny.mq.cache.CommonCache;
import org.tiny.mq.model.EagleMqTopicModel;
import org.tiny.mq.model.QueueModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConsumeQueueAppendHandler {

    public void prepareConsumeQueue(String topicName) throws IOException {
        EagleMqTopicModel eagleMqTopicModel = CommonCache.getEagleMqTopicModelMap().get(topicName);
        List<QueueModel> queueModelList = eagleMqTopicModel.getQueueList();
        List<ConsumeQueueMMapFileModel> consumeQueueMMapFileModels = new ArrayList<>();
        //循环遍历，mmap的初始化
        for (QueueModel queueModel : queueModelList) {
            ConsumeQueueMMapFileModel consumeQueueMMapFileModel = new ConsumeQueueMMapFileModel();
            consumeQueueMMapFileModel.loadFileInMMap(
                    topicName,
                    queueModel.getId(),
                    queueModel.getLastOffset(),
                    queueModel.getLatestOffset().get(),
                    queueModel.getOffsetLimit());
            consumeQueueMMapFileModels.add(consumeQueueMMapFileModel);
        }
        CommonCache.getConsumeQueueMMapFileModelManager().put(topicName, consumeQueueMMapFileModels);
    }
}
