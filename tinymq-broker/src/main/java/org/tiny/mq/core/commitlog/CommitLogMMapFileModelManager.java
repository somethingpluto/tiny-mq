package org.tiny.mq.core.commitlog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommitLogMMapFileModelManager {
    private static Map<String, CommitLogMMapFileModel> memoryMapFileModelMap = new ConcurrentHashMap<>();

    public void put(String topic, CommitLogMMapFileModel commitLogMMapFileModel) {
        memoryMapFileModelMap.put(topic, commitLogMMapFileModel);
    }

    public CommitLogMMapFileModel get(String topic) {
        return memoryMapFileModelMap.get(topic);
    }

}
