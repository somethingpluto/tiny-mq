package org.tiny.mq.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConsumeQueueMMapFileModelManager {

    public Map<String, List<ConsumeQueueMMapFileModel>> consumeQueueMMapFileModel = new HashMap<>();

    public void put(String topic, List<ConsumeQueueMMapFileModel> consumeQueueMMapFileModels) {
        consumeQueueMMapFileModel.put(topic, consumeQueueMMapFileModels);
    }

    public List<ConsumeQueueMMapFileModel> get(String topic) {
        return consumeQueueMMapFileModel.get(topic);
    }
}
