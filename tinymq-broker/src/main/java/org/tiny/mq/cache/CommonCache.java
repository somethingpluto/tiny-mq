package org.tiny.mq.cache;


import io.netty.channel.ChannelHandlerContext;
import org.tiny.mq.config.GlobalProperties;
import org.tiny.mq.core.*;
import org.tiny.mq.model.ConsumeQueueOffsetModel;
import org.tiny.mq.model.EagleMqTopicModel;
import org.tiny.mq.netty.nameserver.HeartBeatTaskManager;
import org.tiny.mq.netty.nameserver.NameServerClient;
import org.tiny.mq.rebalance.ConsumerInstance;
import org.tiny.mq.rebalance.ConsumerInstancePool;
import org.tiny.mq.slave.SlaveSyncService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


public class CommonCache {

    private static NameServerClient nameServerClient = new NameServerClient();
    private static GlobalProperties globalProperties = new GlobalProperties();
    private static List<EagleMqTopicModel> eagleMqTopicModelList = new ArrayList<>();
    private static ConsumeQueueOffsetModel consumeQueueOffsetModel = new ConsumeQueueOffsetModel();
    private static ConsumeQueueMMapFileModelManager consumeQueueMMapFileModelManager = new ConsumeQueueMMapFileModelManager();
    private static CommitLogMMapFileModelManager commitLogMMapFileModelManager = new CommitLogMMapFileModelManager();
    private static HeartBeatTaskManager heartBeatTaskManager = new HeartBeatTaskManager();
    private static CommitLogAppendHandler commitLogAppendHandler;
    private static Map<String, Map<String, List<ConsumerInstance>>> consumeHoldMap = new ConcurrentHashMap<>();
    private static ConsumerInstancePool consumerInstancePool = new ConsumerInstancePool();
    private static ConsumeQueueConsumeHandler consumeQueueConsumeHandler;
    private static SlaveSyncService slaveSyncService;
    private static Map<String, ChannelHandlerContext> slaveChannelMap = new HashMap<>();

    public static SlaveSyncService getSlaveSyncService() {
        return slaveSyncService;
    }

    public static void setSlaveSyncService(SlaveSyncService slaveSyncService) {
        CommonCache.slaveSyncService = slaveSyncService;
    }

    public static void setConsumerInstancePool(ConsumerInstancePool consumerInstancePool) {
        CommonCache.consumerInstancePool = consumerInstancePool;
    }

    public static Map<String, ChannelHandlerContext> getSlaveChannelMap() {
        return slaveChannelMap;
    }

    public static void setSlaveChannelMap(Map<String, ChannelHandlerContext> slaveChannelMap) {
        CommonCache.slaveChannelMap = slaveChannelMap;
    }

    public static ConsumeQueueConsumeHandler getConsumeQueueConsumeHandler() {
        return consumeQueueConsumeHandler;
    }

    public static ConsumeQueueAppendHandler consumeQueueAppendHandler;

    public static ConsumeQueueAppendHandler getConsumeQueueAppendHandler() {
        return consumeQueueAppendHandler;
    }

    public static void setConsumeQueueAppendHandler(ConsumeQueueAppendHandler consumeQueueAppendHandler) {
        CommonCache.consumeQueueAppendHandler = consumeQueueAppendHandler;
    }

    public static void setConsumeQueueConsumeHandler(ConsumeQueueConsumeHandler consumeQueueConsumeHandler) {
        CommonCache.consumeQueueConsumeHandler = consumeQueueConsumeHandler;
    }

    public static ConsumerInstancePool getConsumerInstancePool() {
        return consumerInstancePool;
    }

    public static Map<String, Map<String, List<ConsumerInstance>>> getConsumeHoldMap() {
        return consumeHoldMap;
    }

    public static void setConsumeHoldMap(Map<String, Map<String, List<ConsumerInstance>>> consumeHoldMap) {
        CommonCache.consumeHoldMap = consumeHoldMap;
    }

    public static CommitLogAppendHandler getCommitLogAppendHandler() {
        return commitLogAppendHandler;
    }

    public static void setCommitLogAppendHandler(CommitLogAppendHandler commitLogAppendHandler) {
        CommonCache.commitLogAppendHandler = commitLogAppendHandler;
    }

    public static HeartBeatTaskManager getHeartBeatTaskManager() {
        return heartBeatTaskManager;
    }

    public static void setHeartBeatTaskManager(HeartBeatTaskManager heartBeatTaskManager) {
        CommonCache.heartBeatTaskManager = heartBeatTaskManager;
    }

    public static NameServerClient getNameServerClient() {
        return nameServerClient;
    }

    public static void setNameServerClient(NameServerClient nameServerClient) {
        CommonCache.nameServerClient = nameServerClient;
    }

    public static CommitLogMMapFileModelManager getCommitLogMMapFileModelManager() {
        return commitLogMMapFileModelManager;
    }

    public static void setCommitLogMMapFileModelManager(CommitLogMMapFileModelManager commitLogMMapFileModelManager) {
        CommonCache.commitLogMMapFileModelManager = commitLogMMapFileModelManager;
    }

    public static ConsumeQueueMMapFileModelManager getConsumeQueueMMapFileModelManager() {
        return consumeQueueMMapFileModelManager;
    }

    public static void setConsumeQueueMMapFileModelManager(ConsumeQueueMMapFileModelManager consumeQueueMMapFileModelManager) {
        CommonCache.consumeQueueMMapFileModelManager = consumeQueueMMapFileModelManager;
    }

    public static GlobalProperties getGlobalProperties() {
        return globalProperties;
    }

    public static void setGlobalProperties(GlobalProperties globalProperties) {
        CommonCache.globalProperties = globalProperties;
    }

    public static Map<String, EagleMqTopicModel> getEagleMqTopicModelMap() {
        return eagleMqTopicModelList.stream().collect(Collectors.toMap(EagleMqTopicModel::getTopic, item -> item));
    }

    public static List<EagleMqTopicModel> getEagleMqTopicModelList() {
        return eagleMqTopicModelList;
    }

    public static void setEagleMqTopicModelList(List<EagleMqTopicModel> eagleMqTopicModelList) {
        CommonCache.eagleMqTopicModelList = eagleMqTopicModelList;
    }

    public static ConsumeQueueOffsetModel getConsumeQueueOffsetModel() {
        return consumeQueueOffsetModel;
    }

    public static void setConsumeQueueOffsetModel(ConsumeQueueOffsetModel consumeQueueOffsetModel) {
        CommonCache.consumeQueueOffsetModel = consumeQueueOffsetModel;
    }
}
