package org.tiny.mq.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.common.constants.BrokerConstants;
import org.tiny.mq.model.ConfigModel;
import org.tiny.mq.model.consumequeue.ConsumeQueueOffsetModel;
import org.tiny.mq.pool.ConsumeQueueFreshThreadPool;
import org.tiny.mq.utils.FileContentUtil;

import java.util.concurrent.TimeUnit;

public class ConsumeQueueOffsetPropertiesLoader {

    private final Logger logger = LoggerFactory.getLogger(ConsumeQueueOffsetPropertiesLoader.class);
    private String filePath;

    public void loadProperties() {
        ConfigModel config = GlobalCache.getConfig();
        String basePath = config.getBasePath();
        if (StringUtil.isNullOrEmpty(basePath)) {
            throw new IllegalArgumentException("TINY_MQ_HOME is invalid");
        }
        filePath = basePath + "/broker/config/consumequeue-offset.json";
        String fileContent = FileContentUtil.readFromFile(filePath);
        ConsumeQueueOffsetModel consumeQueueOffsetModel = JSON.parseObject(fileContent, ConsumeQueueOffsetModel.class);
        GlobalCache.setConsumeQueueOffsetModel(consumeQueueOffsetModel);
    }

    /**
     * 开启一个刷新磁盘任务
     */
    public void startRefreshConsumeQueueOffsetTask() {
        ConsumeQueueFreshThreadPool.refreshConsumeQueueOffsetExecutor.execute(new Runnable() {
            @Override
            public void run() {
                do {
                    try {
                        TimeUnit.SECONDS.sleep(BrokerConstants.DEFAULT_REFRESH_CONSUME_QUEUE_OFFSET_TIME_STEP);
                        ConsumeQueueOffsetModel consumeQueueOffsetModel = GlobalCache.getConsumeQueueOffsetModel();
                        FileContentUtil.overWriteToFile(filePath, JSON.toJSONString(consumeQueueOffsetModel, SerializerFeature.PrettyFormat));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } while (true);
            }
        });
    }
}
