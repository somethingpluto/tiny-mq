package org.tiny.mq.core.consumequeue;

import org.tiny.mq.config.GlobalCache;
import org.tiny.mq.core.commitlog.CommitLogMMapFileModel;
import org.tiny.mq.model.*;
import org.tiny.mq.model.commitlog.TopicModel;
import org.tiny.mq.model.consumequeue.ConsumeQueueItemModel;
import org.tiny.mq.model.consumequeue.ConsumeQueueOffsetModel;
import org.tiny.mq.model.consumequeue.QueueModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class ConsumeQueueConsumeHandler {
    private final ReentrantLock ackMessageLock = new ReentrantLock(false);


    /**
     * 读取当前最新一条consumeQueue的消息内容(读的是索引)
     *
     * @param topicName
     * @param consumerGroupName
     * @param queueId
     * @return
     */
    public byte[] consume(String topicName, String consumerGroupName, Integer queueId) {
        TopicModel topicModel = ModelManager.getTopicModel(topicName);
        ConsumeQueueOffsetModel.ConsumeGroupInfo topicConsumerGroup = ModelManager.getTopicConsumerGroup(topicName);
        Map<String, Map<String, String>> consumeGroup = topicConsumerGroup.getConsumeGroup();
        Map<String, String> queueInfoMap = consumeGroup.get(consumerGroupName);
        List<QueueModel> queueList = topicModel.getQueueList();
        // 如果这个消费组是首次消费 则创建一个消费组信息
        if (queueInfoMap == null) {
            queueInfoMap = new HashMap<>();
            for (QueueModel queueModel : queueList) {
                queueInfoMap.put(String.valueOf(queueModel.getId()), "00000000#0");
            }
            consumeGroup.put(consumerGroupName, queueInfoMap);
        }
        // 根据queueId 获取指定的队列信息
        String offsetInfo = queueInfoMap.get(String.valueOf(queueId));
        String[] offsetAttr = offsetInfo.split("#");
        int consumeQueueOffset = Integer.parseInt(offsetAttr[1]);
        QueueModel queueModel = queueList.get(queueId);
        // 检查是否队列已经消费到头了 到头了就算了 不能继续消费
        if (queueModel.getLatestOffset().get() <= consumeQueueOffset) {
            return null;
        }
        // 根据queueId 获取topic下队列的信息 并进行更新
        List<ConsumeQueueMemoryMapFileModel> consumeQueueMemoryMapFileModels = GlobalCache.getConsumeQueueMMapFileModelManager().get(topicName);
        ConsumeQueueMemoryMapFileModel consumeQueueMemoryMapFileModel = consumeQueueMemoryMapFileModels.get(queueId);
        // 读取队列索引的文件内容-这个内容就是真实信息在commitlog下的位置
        byte[] bytes = consumeQueueMemoryMapFileModel.readContent(consumeQueueOffset);
        ConsumeQueueItemModel cnsumeQueueItemModel = new ConsumeQueueItemModel();
        cnsumeQueueItemModel.buildFromBytes(bytes);

        // 获取对应的log mmap 从中读取数据
        CommitLogMMapFileModel commitLogMMapFileModel = GlobalCache.getMemoryMapFileModelManager().get(topicName);
        byte[] result = commitLogMMapFileModel.readContent(cnsumeQueueItemModel.getMsgIndex(), cnsumeQueueItemModel.getMsgLength());
        return result;
    }

    public boolean ack(String topicName, String consumeGroup, Integer queueId) {
        try {
            Map<String, ConsumeQueueOffsetModel.ConsumeGroupInfo> consumeGroupInfoMap = GlobalCache.getConsumeQueueOffsetModel().getOffsetTable();
            ConsumeQueueOffsetModel.ConsumeGroupInfo topicConsumerGroupInfo = consumeGroupInfoMap.get(topicName);
            Map<String, String> queueInfoMap = topicConsumerGroupInfo.getConsumeGroup().get(consumeGroup);
            String queueInfo = queueInfoMap.get(String.valueOf(queueId));
            String[] queueAttr = queueInfo.split("#");
            String fileName = queueAttr[0];
            Integer currentOffset = Integer.parseInt(queueAttr[1]);
            currentOffset += 12;
            queueInfoMap.put(String.valueOf(queueId), fileName + "#" + currentOffset);
        } catch (Exception e) {
            System.out.println("ack 异常");
            e.printStackTrace();
        } finally {

        }
        return true;

    }


}
