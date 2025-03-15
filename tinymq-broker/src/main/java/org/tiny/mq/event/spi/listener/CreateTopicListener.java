package org.tiny.mq.event.spi.listener;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.cache.CommonCache;
import org.tiny.mq.common.coder.TcpMsg;
import org.tiny.mq.common.constants.BrokerConstants;
import org.tiny.mq.common.dto.CreateTopicReqDTO;
import org.tiny.mq.common.enums.BrokerClusterModeEnum;
import org.tiny.mq.common.enums.BrokerEventCode;
import org.tiny.mq.common.enums.BrokerRegistryEnum;
import org.tiny.mq.common.enums.BrokerResponseCode;
import org.tiny.mq.common.event.Listener;
import org.tiny.mq.event.model.CreateTopicEvent;
import org.tiny.mq.model.CommitLogModel;
import org.tiny.mq.model.EagleMqTopicModel;
import org.tiny.mq.model.QueueModel;
import org.tiny.mq.utils.LogFileNameUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CreateTopicListener implements Listener<CreateTopicEvent> {

    private static final Logger logger = LoggerFactory.getLogger(CreateTopicListener.class);

    @Override
    public void onReceive(CreateTopicEvent event) throws Exception {
        CreateTopicReqDTO createTopicReqDTO = event.getCreateTopicReqDTO();
        String topicName = createTopicReqDTO.getTopicName();
        Integer queueSize = createTopicReqDTO.getQueueSize();
        EagleMqTopicModel eagleMqTopicModel = CommonCache.getEagleMqTopicModelMap().get(topicName);
        if (eagleMqTopicModel != null) {
            TcpMsg tcpMsg = new TcpMsg(BrokerResponseCode.TOPIC_ALREADY_EXIST.getCode(), "tpoic alread exist".getBytes());
            event.getChannelHandlerContext().writeAndFlush(tcpMsg);
        }
        if (queueSize > 100) {
            TcpMsg tcpMsg = new TcpMsg(BrokerResponseCode.QUEUE_SIZE_TOO_LARGE.getCode(), BrokerResponseCode.QUEUE_SIZE_TOO_LARGE.getDesc().getBytes());
            event.getChannelHandlerContext().writeAndFlush(tcpMsg);
        }
        createTopicFile(topicName);
        createTopicQueue(topicName, queueSize);
        addToCommonCache(topicName, queueSize);
        loadFileToMMap(topicName);
        logger.info("topic:{} create success", topicName);
        // 检测是否是主节点 主节点需要将该操作同步给从节点
        if (CommonCache.getGlobalProperties().getBrokerClusterMode().equals(BrokerClusterModeEnum.MASTER_SLAVE.getDesc()) && CommonCache.getGlobalProperties().getBrokerClusterRole().equals(BrokerRegistryEnum.MASTER.getDesc())) {
            logger.info("master sync create topic to slave node");
            for (ChannelHandlerContext slaveChannel : CommonCache.getSlaveChannelMap().values()) {
                TcpMsg tcpMsg = new TcpMsg(BrokerEventCode.CREATE_TOPIC.getCode(), JSON.toJSONBytes(createTopicReqDTO));
                slaveChannel.writeAndFlush(tcpMsg);
            }

        }
    }

    private static void createTopicFile(String topicName) throws IOException {
        String baseCommitLogDirPath = LogFileNameUtil.buildCommitLogBasePath(topicName);
        File commitLogDir = new File(baseCommitLogDirPath);
        commitLogDir.mkdir();
        File initCommitLogFile = new File(baseCommitLogDirPath + BrokerConstants.SPLIT + LogFileNameUtil.buildFirstCommitLogName());
        initCommitLogFile.createNewFile();
        logger.info("create commit log file:{}", initCommitLogFile.getPath());
    }

    private static void createTopicQueue(String topicName, int queueSize) throws IOException {
        String consumeQueueBasePath = LogFileNameUtil.buildConsumeQueueuBasePath(topicName);
        File consumeQueueDir = new File(consumeQueueBasePath);
        consumeQueueDir.mkdir();
        for (int i = 0; i < queueSize; i++) {
            String queueDirPath = consumeQueueDir + BrokerConstants.SPLIT + i;
            File queueDirFile = new File(queueDirPath);
            queueDirFile.mkdir();
            // 创建consume queue 文件
            File consumeQueueFile = new File(queueDirPath + BrokerConstants.SPLIT + LogFileNameUtil.buildFirstConsumeQueue());
            consumeQueueFile.createNewFile();
            logger.info("topic:{} create consume queue file:{}", topicName, consumeQueueFile.getPath());
        }
    }

    private static void loadFileToMMap(String topicName) throws IOException {
        CommonCache.getCommitLogAppendHandler().prepareMMapLoading(topicName);
        CommonCache.getConsumeQueueAppendHandler().prepareConsumeQueue(topicName);
    }

    private static void addToCommonCache(String topicName, int queueSize) {
        EagleMqTopicModel eagleMqTopicModel = new EagleMqTopicModel();
        eagleMqTopicModel.setTopic(topicName);
        eagleMqTopicModel.setCreateAt(System.currentTimeMillis());
        eagleMqTopicModel.setUpdateAt(System.currentTimeMillis());
        CommitLogModel commitLogModel = new CommitLogModel();
        commitLogModel.setFileName(LogFileNameUtil.buildFirstCommitLogName());
        commitLogModel.setOffset(new AtomicInteger(0));
        commitLogModel.setOffsetLimit(BrokerConstants.COMMIT_LOG_DEFAULT_MMAP_SIZE.longValue());
        eagleMqTopicModel.setCommitLogModel(commitLogModel);
        List<QueueModel> queueModelList = new ArrayList<>();
        for (int i = 0; i < queueSize; i++) {
            QueueModel queueModel = new QueueModel();
            queueModel.setId(i);
            queueModel.setFileName(LogFileNameUtil.buildFirstConsumeQueue());
            queueModel.setOffsetLimit(BrokerConstants.QUEUE_DEFAULT_SIZE);
            queueModel.setLastOffset(0);
            queueModel.setLatestOffset(new AtomicInteger(0));
            queueModelList.add(queueModel);
        }
        eagleMqTopicModel.setQueueList(queueModelList);
        CommonCache.getEagleMqTopicModelList().add(eagleMqTopicModel);
    }

    public static void main(String[] args) throws IOException {
        CommonCache.getGlobalProperties().setEagleMqHome("E:\\code\\code_back\\Java_project\\tiny-frame\\tinymq\\broker");
//        createTopicFile("order_topic");
        createTopicQueue("order_topic", 3);
    }
}
