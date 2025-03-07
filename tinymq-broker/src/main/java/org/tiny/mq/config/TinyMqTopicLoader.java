package org.tiny.mq.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.netty.util.internal.StringUtil;
import org.tiny.mq.cache.CommonCache;
import org.tiny.mq.common.constants.BrokerConstants;
import org.tiny.mq.model.EagleMqTopicModel;
import org.tiny.mq.utils.FileContentUtil;

import java.util.List;
import java.util.concurrent.TimeUnit;


public class TinyMqTopicLoader {

    private String filePath;

    public void loadProperties() {
        GlobalProperties globalProperties = CommonCache.getGlobalProperties();
        String basePath = globalProperties.getEagleMqHome();
        if (StringUtil.isNullOrEmpty(basePath)) {
            throw new IllegalArgumentException("TINY_MQ_HOME is invalid!");
        }
        filePath = basePath + "/config/tinymq-topic.json";
        String fileContent = FileContentUtil.readFromFile(filePath);
        List<EagleMqTopicModel> eagleMqTopicModelList = JSON.parseArray(fileContent, EagleMqTopicModel.class);
        CommonCache.setEagleMqTopicModelList(eagleMqTopicModelList);
    }

    /**
     * 开启一个刷新内存到磁盘的任务
     */
    public void startRefreshEagleMqTopicInfoTask() {
        //异步线程
        //每3秒将内存中的配置刷新到磁盘里面
        CommonThreadPoolConfig.refreshEagleMqTopicExecutor.execute(new Runnable() {
            @Override
            public void run() {
                do {
                    try {
                        TimeUnit.SECONDS.sleep(BrokerConstants.DEFAULT_REFRESH_MQ_TOPIC_TIME_STEP);
                        List<EagleMqTopicModel> eagleMqTopicModelList = CommonCache.getEagleMqTopicModelList();
                        FileContentUtil.overWriteToFile(filePath, JSON.toJSONString(eagleMqTopicModelList, SerializerFeature.PrettyFormat));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } while (true);
            }
        });

    }
}
