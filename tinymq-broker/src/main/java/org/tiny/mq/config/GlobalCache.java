package org.tiny.mq.config;

import org.tiny.mq.core.commitlog.CommitLogMMapFileModelManager;
import org.tiny.mq.core.consumequeue.ConsumeQueueMMapFileModelManager;
import org.tiny.mq.model.ConfigModel;
import org.tiny.mq.model.commitlog.TopicModel;
import org.tiny.mq.model.consumequeue.ConsumeQueueOffsetModel;
import org.tiny.mq.model.nameserver.NameServerConfigModel;
import org.tiny.mq.netty.nameserver.NameServerClient;
import org.tiny.mq.netty.nameserver.manager.HeartBeatManager;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GlobalCache {

    private static ConfigModel config;
    private static List<TopicModel> topicConfigList;
    private static ConsumeQueueOffsetModel consumeQueueOffsetModel;
    private static ConsumeQueueMMapFileModelManager consumeQueueMMapFileModelManager = new ConsumeQueueMMapFileModelManager();
    private static CommitLogMMapFileModelManager commitLogMMapFileModelManager = new CommitLogMMapFileModelManager();
    private static HeartBeatManager heartBeatManager = new HeartBeatManager();    private static NameServerConfigModel nameServerConfig = NameServerConfigLoader.loadProperties();
    private static NameServerClient nameServerClient = new NameServerClient();

    public static ConfigModel getConfig() {
        return config;
    }

    public static void setConfig(ConfigModel config) {
        GlobalCache.config = config;
    }

    public static List<TopicModel> getTopicConfigList() {
        return topicConfigList;
    }

    public static void setTopicConfigList(List<TopicModel> topicConfigList) {
        GlobalCache.topicConfigList = topicConfigList;
    }

    public static Map<String, TopicModel> getMQTopicModelMap() {
        return topicConfigList.stream().collect(Collectors.toMap(TopicModel::getTopicName, item -> item));
    }

    public static ConsumeQueueOffsetModel getConsumeQueueOffsetModel() {
        return consumeQueueOffsetModel;
    }

    public static void setConsumeQueueOffsetModel(ConsumeQueueOffsetModel consumeQueueOffsetModel) {
        GlobalCache.consumeQueueOffsetModel = consumeQueueOffsetModel;
    }

    public static ConsumeQueueMMapFileModelManager getConsumeQueueMMapFileModelManager() {
        return consumeQueueMMapFileModelManager;
    }

    public static void setConsumeQueueMMapFileModelManager(ConsumeQueueMMapFileModelManager consumeQueueMMapFileModelManager) {
        GlobalCache.consumeQueueMMapFileModelManager = consumeQueueMMapFileModelManager;
    }

    public static CommitLogMMapFileModelManager getMemoryMapFileModelManager() {
        return commitLogMMapFileModelManager;
    }

    public static void setMemoryMapFileModelManager(CommitLogMMapFileModelManager commitLogMMapFileModelManager) {
        GlobalCache.commitLogMMapFileModelManager = commitLogMMapFileModelManager;
    }

    public static CommitLogMMapFileModelManager getCommitLogMMapFileModelManager() {
        return commitLogMMapFileModelManager;
    }

    public static void setCommitLogMMapFileModelManager(CommitLogMMapFileModelManager commitLogMMapFileModelManager) {
        GlobalCache.commitLogMMapFileModelManager = commitLogMMapFileModelManager;
    }

    public static NameServerConfigModel getNameServerConfig() {
        return nameServerConfig;
    }

    public static void setNameServerConfig(NameServerConfigModel nameServerConfig) {
        GlobalCache.nameServerConfig = nameServerConfig;
    }

    public static NameServerClient getNameServerClient() {
        return nameServerClient;
    }

    public static void setNameServerClient(NameServerClient nameServerClient) {
        GlobalCache.nameServerClient = nameServerClient;
    }

    public static HeartBeatManager getHeartBeatManager() {
        return heartBeatManager;
    }

    public static void setHeartBeatManager(HeartBeatManager heartBeatManager) {
        GlobalCache.heartBeatManager = heartBeatManager;
    }


}
