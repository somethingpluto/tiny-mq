package org.tiny.mq.core.consumequeue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConsumeQueueMMapFileModelManager {


    public Map<String, List<ConsumeQueueMemoryMapFileModel>> consumeQueueMMapFileModel = new HashMap<>();

    public void put(String topic,List<ConsumeQueueMemoryMapFileModel> consumeQueueMemoryMapFileModels) {
        consumeQueueMMapFileModel.put(topic,consumeQueueMemoryMapFileModels);
    }

    public List<ConsumeQueueMemoryMapFileModel> get(String topic) {
        return consumeQueueMMapFileModel.get(topic);
    }
}
