package org.tiny.mq.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.common.constants.BrokerConstants;
import org.tiny.mq.model.ConfigModel;
import org.tiny.mq.model.commitlog.TopicModel;
import org.tiny.mq.model.consumequeue.QueueModel;
import org.tiny.mq.pool.CommitLogFreshThreadPool;
import org.tiny.mq.utils.FileContentUtil;
import org.tiny.mq.utils.FileNameUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CommitLogPropertiesLoader {
    private final Logger logger = LoggerFactory.getLogger(CommitLogPropertiesLoader.class);
    private String filePath;


    public void loadProperties() {
        ConfigModel config = GlobalCache.getConfig();
        String basePath = config.getBasePath();
        filePath = basePath + "/broker/config/tinymq-topic.json";
        logger.info("topic file path is {}", filePath);
        String content = FileContentUtil.readFromFile(filePath);
        List<TopicModel> topicInfoList = JSON.parseArray(content, TopicModel.class);
        this.initCommitLogFileAndQueue(topicInfoList);
        GlobalCache.setTopicConfigList(topicInfoList);
    }

    private void initCommitLogFileAndQueue(List<TopicModel> topicInfoList) {
        topicInfoList.forEach(topicModel -> {
            String fileName = topicModel.getCommitLogModel().getFileName();
            if (fileName.equals(BrokerConstants.INIT_COMMITLOG_FILE_NAME)) {
                String initFilePath = FileNameUtil.buildCommitLogFilePath(topicModel.getTopicName(), fileName);
                File file = new File(initFilePath);
                boolean isExists = false;
                try {
                    isExists = FileNameUtil.createFile(file);
                    if (!isExists) {
                        topicModel.getCommitLogModel().setOffset(new AtomicInteger(0));
                        logger.info("topicName [{}] init commit log file [{}] create failed", topicModel.getTopicName(), initFilePath);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            // 检查索引队列文件
            List<QueueModel> queueList = topicModel.getQueueList();
            queueList.forEach(queueModel -> {
                String queueModelFileName = queueModel.getFileName();
                if (queueModelFileName.equals(BrokerConstants.INIT_COMMITLOG_FILE_NAME)) {
                    String consumeQueueFilePath = FileNameUtil.buildConsumeQueueFilePath(topicModel.getTopicName(), queueModel.getId(), queueModelFileName);
                    File file = new File(consumeQueueFilePath);
                    boolean isExists = false;
                    try {
                        isExists = FileNameUtil.createFile(file);
                        // 文件不存在 则进行初始化工作
                        if (!isExists) {
                            queueModel.setLastOffset(0);
                            queueModel.setLatestOffset(new AtomicInteger(0));
                            logger.info("topicName [{}] init queue file [{}] queueId [{}] create failed", topicModel.getTopicName(), consumeQueueFilePath, queueModel.getId());
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        });
    }

    public void startRefreshMQTopicInfoTask() {
        CommitLogFreshThreadPool.refreshMQTopicExecutor.execute(() -> {
            do {
                try {
                    TimeUnit.SECONDS.sleep(BrokerConstants.DEFAULT_REFRESH_MQ_TOPIC_TIME_SETP);
                    List<TopicModel> topicConfigList = GlobalCache.getTopicConfigList();
                    FileContentUtil.overWriteToFile(filePath, JSON.toJSONString(topicConfigList, SerializerFeature.PrettyFormat));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } while (true);
        });
    }
}
