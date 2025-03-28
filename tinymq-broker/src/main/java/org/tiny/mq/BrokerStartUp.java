package org.tiny.mq;


import org.tiny.mq.cache.CommonCache;
import org.tiny.mq.common.enums.BrokerClusterModeEnum;
import org.tiny.mq.common.event.EventBus;
import org.tiny.mq.config.ConsumeQueueOffsetLoader;
import org.tiny.mq.config.GlobalPropertiesLoader;
import org.tiny.mq.config.TinyMqTopicLoader;
import org.tiny.mq.core.CommitLogAppendHandler;
import org.tiny.mq.core.ConsumeQueueAppendHandler;
import org.tiny.mq.core.ConsumeQueueConsumeHandler;
import org.tiny.mq.model.EagleMqTopicModel;
import org.tiny.mq.netty.broker.BrokerServer;
import org.tiny.mq.slave.SlaveSyncService;
import org.tiny.mq.timewheel.TimeWheelModelManager;

import java.io.IOException;


public class BrokerStartUp {

    private static GlobalPropertiesLoader globalPropertiesLoader;
    private static TinyMqTopicLoader eagleMqTopicLoader;
    private static CommitLogAppendHandler commitLogAppendHandler;
    private static ConsumeQueueOffsetLoader consumeQueueOffsetLoader;
    private static ConsumeQueueAppendHandler consumeQueueAppendHandler;
    private static ConsumeQueueConsumeHandler consumeQueueConsumeHandler;

    /**
     * 初始化配置逻辑
     */
    private static void initProperties() throws IOException {
        globalPropertiesLoader = new GlobalPropertiesLoader();
        eagleMqTopicLoader = new TinyMqTopicLoader();
        consumeQueueOffsetLoader = new ConsumeQueueOffsetLoader();
        consumeQueueConsumeHandler = new ConsumeQueueConsumeHandler();
        commitLogAppendHandler = new CommitLogAppendHandler();
        consumeQueueAppendHandler = new ConsumeQueueAppendHandler();

        globalPropertiesLoader.loadProperties();
        eagleMqTopicLoader.loadProperties();
        eagleMqTopicLoader.startRefreshEagleMqTopicInfoTask();
        consumeQueueOffsetLoader.loadProperties();
        consumeQueueOffsetLoader.startRefreshConsumeQueueOffsetTask();
        for (EagleMqTopicModel eagleMqTopicModel : CommonCache.getEagleMqTopicModelMap().values()) {
            String topicName = eagleMqTopicModel.getTopic();
            commitLogAppendHandler.prepareMMapLoading(topicName);
            consumeQueueAppendHandler.prepareConsumeQueue(topicName);
        }
        CommonCache.setConsumeQueueConsumeHandler(consumeQueueConsumeHandler);
        CommonCache.setCommitLogAppendHandler(commitLogAppendHandler);
        CommonCache.setConsumeQueueAppendHandler(consumeQueueAppendHandler);
    }

    /**
     * 初始化和nameserver的长链接通道
     */
    private static void initNameServerChannel() {
        CommonCache.getNameServerClient().initConnection();
        CommonCache.getNameServerClient().sendRegistryMsg();
        if (!BrokerClusterModeEnum.MASTER_SLAVE.getDesc().equals(CommonCache.getGlobalProperties().getBrokerClusterMode()) || "master".equals(CommonCache.getGlobalProperties().getBrokerClusterRole())) {
            return;
        }
        // 获取主节点的ip
        String address = CommonCache.getNameServerClient().queryBrokerClusterMaster();
        SlaveSyncService slaveSyncService = new SlaveSyncService();
        slaveSyncService.connectToMasterBrokerNode(address);
        slaveSyncService.sendStartSyncMsg();
    }

    //开启重平衡任务
    private static void initReBalanceJob() {
        CommonCache.getConsumerInstancePool().startReBalanceJob();
    }

    private static void initTimeWheel() {
        TimeWheelModelManager timeWheelModelManager = CommonCache.getTimeWheelModelManager();
        timeWheelModelManager.init(new EventBus("time-wheel-event-bus"));
        timeWheelModelManager.doScanTask();
    }

    private static void initBrokerServer() throws InterruptedException {
        BrokerServer brokerServer = new BrokerServer(CommonCache.getGlobalProperties().getBrokerPort());
        brokerServer.startServer();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        //加载配置 ，缓存对象的生成
        initProperties();
        initNameServerChannel();
        initReBalanceJob();
        initTimeWheel();
        //这个函数是会阻塞的
        initBrokerServer();
    }
}
