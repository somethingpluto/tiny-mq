package org.tiny.mq.hook;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.tiny.mq.config.GlobalCache;
import org.tiny.mq.model.commitlog.TopicModel;
import org.tiny.mq.model.consumequeue.ConsumeQueueOffsetModel;
import org.tiny.mq.utils.FileContentUtil;

import java.util.List;

public class ShutDoneHook extends Thread {

    /**
     * 刷新commitLog 到磁盘
     */
    private void commitLogFileToDisk() {
        String basePath = GlobalCache.getConfig().getBasePath();
        String filePath = basePath + "/broker/config/tinymq-topic.json";
        List<TopicModel> topicConfigList = GlobalCache.getTopicConfigList();
        FileContentUtil.overWriteToFile(filePath, JSON.toJSONString(topicConfigList, SerializerFeature.PrettyFormat));
    }

    /**
     * 刷新consumeQueue到磁盘
     */
    private void consumeQueueFileToDisk() {
        String basePath = GlobalCache.getConfig().getBasePath();
        String filePath = basePath + "/broker/config/consumequeue-offset.json";
        ConsumeQueueOffsetModel consumeQueueOffsetModel = GlobalCache.getConsumeQueueOffsetModel();
        FileContentUtil.overWriteToFile(filePath, JSON.toJSONString(consumeQueueOffsetModel, SerializerFeature.PrettyFormat));
    }

    @Override
    public void run() {
        this.commitLogFileToDisk();
        this.consumeQueueFileToDisk();
        System.out.println("all file to disk");
    }
}
