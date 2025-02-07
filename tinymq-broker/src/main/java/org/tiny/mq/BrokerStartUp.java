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
import org.tiny.mq.netty.nameserver.NameServerClient;

import java.io.IOException;
import java.util.List;

public class BrokerStartUp {

    private static final Logger logger = LoggerFactory.getLogger(BrokerStartUp.class);
    private static final CommitLogAppenderHandler messageAppenderHandler = new CommitLogAppenderHandler();
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
            messageAppenderHandler.prepareMMapLoading(topicModel);
            consumeQueueAppendHandler.prepareConsumeQueue(topicModel.getTopicName());
        }
    }

    public static void initNameServerChannel() {
        NameServerClient client = GlobalCache.getNameServerClient();
        client.initConnection();
        client.sendRegistryMessage();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Runtime.getRuntime().addShutdownHook(new ShutDoneHook());
        //加载配置 ，缓存对象的生成
        initProperties();
        // 初始化网络连接
        initNameServerChannel();
        //模拟初始化文件映射
//        String topic = "user_topic";
//        String userInfoConsumerGroup = "user_info_consumer";
//        String orderServiceConsumerGroup = "order_service_group";

//        AtomicInteger i = new AtomicInteger();
//        new Thread(() -> {
//            while (true) {
//                try {
//                    messageAppenderHandler.appendMessage(topic, ("message_" + (i.getAndIncrement())).getBytes());
//                    TimeUnit.MILLISECONDS.sleep(10);
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        }).start();
//
//        Thread.sleep(10000);
//        new Thread(() -> {
//            while (true) {
//                byte[] content = consumeQueueConsumeHandler.consume(topic, userInfoConsumerGroup, 0);
//                if (content != null && content.length != 0) {
//                    System.out.println(userInfoConsumerGroup + ",消费内容:" + new String(content));
//                    consumeQueueConsumeHandler.ack(topic, userInfoConsumerGroup, 0);
//                } else {
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        }).start();
//
//        new Thread(() -> {
//            while (true) {
//                byte[] content = consumeQueueConsumeHandler.consume(topic, orderServiceConsumerGroup, 0);
//                if (content != null) {
//                    System.out.println(orderServiceConsumerGroup + ",消费内容:" + new String(content));
//                    consumeQueueConsumeHandler.ack(topic, orderServiceConsumerGroup, 0);
//                } else {
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        }).start();
    }
}
