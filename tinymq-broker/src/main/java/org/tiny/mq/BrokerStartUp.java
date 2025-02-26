package org.tiny.mq;

import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.config.*;
import org.tiny.mq.core.commitlog.CommitLogAppenderHandler;
import org.tiny.mq.core.consumequeue.ConsumeQueueAppendHandler;
import org.tiny.mq.core.consumequeue.ConsumeQueueConsumeHandler;
import org.tiny.mq.hook.ShutDoneHook;
import org.tiny.mq.model.commitlog.TopicModel;
import org.tiny.mq.netty.broker.BrokerServer;
import org.tiny.mq.netty.nameserver.NameServerClient;

import java.io.IOException;
import java.util.List;

public class BrokerStartUp {

    private static final Logger logger = LoggerFactory.getLogger(BrokerStartUp.class);
    private static final CommitLogAppenderHandler commitLogAppenderHandler = new CommitLogAppenderHandler();
    private static final ConfigPropertiesLoader configPropertiesLoader = new ConfigPropertiesLoader();
    private static final CommitLogPropertiesLoader commitLogPropertiesLoader = new CommitLogPropertiesLoader();
    private static final ConsumeQueueOffsetPropertiesLoader consumeQueueOffsetPropertiesLoader = new ConsumeQueueOffsetPropertiesLoader();
    private static final ConsumeQueueAppendHandler consumeQueueAppendHandler = new ConsumeQueueAppendHandler();
    private static final ConsumeQueueConsumeHandler consumeQueueConsumeHandler = new ConsumeQueueConsumeHandler();


    public static void initProperties() throws IOException {
        NameServerConfigLoader.loadProperties();
        configPropertiesLoader.loadProperties();
        commitLogPropertiesLoader.loadProperties();
        consumeQueueOffsetPropertiesLoader.loadProperties();

        commitLogPropertiesLoader.startRefreshMQTopicInfoTask();
        consumeQueueOffsetPropertiesLoader.startRefreshConsumeQueueOffsetTask();
        String basePath = GlobalCache.getConfig().getBasePath();
        if (StringUtil.isNullOrEmpty(basePath)) {
            throw new IllegalArgumentException("tiny_mq_home_path is invalid");
        }

        List<TopicModel> topicConfigList = GlobalCache.getTopicConfigList();
        for (TopicModel topicModel : topicConfigList) {
            commitLogAppenderHandler.prepareMMapLoading(topicModel);
            consumeQueueAppendHandler.prepareConsumeQueue(topicModel.getTopicName());
        }
        GlobalCache.setCommitLogAppenderHandler(commitLogAppenderHandler);
    }

    public static void initNameServerChannel() {
        NameServerClient client = GlobalCache.getNameServerClient();
        client.initConnection();
        client.sendRegistryMessage();
    }

    public static void initBrokerServerChannel() throws InterruptedException {
        BrokerServer brokerServer = new BrokerServer(GlobalCache.getNameServerConfig().getBrokerPort());
        brokerServer.startServer();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Runtime.getRuntime().addShutdownHook(new ShutDoneHook());
        //加载配置 ，缓存对象的生成
        initProperties();
        // 初始化网络连接
        initNameServerChannel();
        // 初始化Broker服务
        initBrokerServerChannel();
    }
}
