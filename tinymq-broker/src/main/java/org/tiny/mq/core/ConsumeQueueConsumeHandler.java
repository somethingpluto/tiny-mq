package org.tiny.mq.core;


import org.tiny.mq.cache.CommonCache;
import org.tiny.mq.common.dto.ConsumeMsgCommitLogDTO;
import org.tiny.mq.model.*;
import org.tiny.mq.utils.AckMessageLock;
import org.tiny.mq.utils.UnfailReentrantLock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ConsumeQueueConsumeHandler {

    public AckMessageLock ackMessageLock = new UnfailReentrantLock();


    public List<ConsumeMsgCommitLogDTO> consume(ConsumeQueueConsumeReqModel consumeQueueConsumeReqModel) {
        String topic = consumeQueueConsumeReqModel.getTopic();
        //1.检查参数合法性
        //2.获取当前匹配的队列的最新的consumeQueue的offset是多少
        //3.获取当前匹配的队列存储文件的mmap对象，然后读取offset地址的数据
        EagleMqTopicModel eagleMqTopicModel = CommonCache.getEagleMqTopicModelMap().get(topic);
        if (eagleMqTopicModel == null) {
            throw new RuntimeException("topic " + topic + " not exist!");
        }
        String consumeGroup = consumeQueueConsumeReqModel.getConsumeGroup();
        Integer queueId = consumeQueueConsumeReqModel.getQueueId();
        Integer batchSize = consumeQueueConsumeReqModel.getBatchSize();

        ConsumeQueueOffsetModel.OffsetTable offsetTable = CommonCache.getConsumeQueueOffsetModel().getOffsetTable();
        Map<String, ConsumeQueueOffsetModel.ConsumerGroupDetail> consumerGroupDetailMap = offsetTable.getTopicConsumerGroupDetail();
        ConsumeQueueOffsetModel.ConsumerGroupDetail consumerGroupDetail = consumerGroupDetailMap.get(topic);
        //如果是首次消费
        if (consumerGroupDetail == null) {
            consumerGroupDetail = new ConsumeQueueOffsetModel.ConsumerGroupDetail();
            consumerGroupDetailMap.put(topic, consumerGroupDetail);
        }
        Map<String, Map<String, String>> consumeGroupOffsetMap = consumerGroupDetail.getConsumerGroupDetailMap();
        Map<String, String> queueOffsetDetailMap = consumeGroupOffsetMap.get(consumeGroup);
        List<QueueModel> queueList = eagleMqTopicModel.getQueueList();
        if (queueOffsetDetailMap == null) {
            queueOffsetDetailMap = new HashMap<>();
            for (QueueModel queueModel : queueList) {
                queueOffsetDetailMap.put(String.valueOf(queueModel.getId()), "00000000#0");
            }
            consumeGroupOffsetMap.put(consumeGroup, queueOffsetDetailMap);
        }
        String offsetStrInfo = queueOffsetDetailMap.get(String.valueOf(queueId));
        String[] offsetStrArr = offsetStrInfo.split("#");
        Integer consumeQueueOffset = Integer.valueOf(offsetStrArr[1]);
        QueueModel queueModel = queueList.get(queueId);
        //消费到了尽头
        if (queueModel.getLatestOffset().get() <= consumeQueueOffset) {
            return null;
        }
        List<ConsumeQueueMMapFileModel> consumeQueueOffsetModels = CommonCache.getConsumeQueueMMapFileModelManager().get(topic);
        ConsumeQueueMMapFileModel consumeQueueMMapFileModel = consumeQueueOffsetModels.get(queueId);
        //一次读取多条consumeQueue的数据内容
        List<byte[]> consumeQueueContentList = consumeQueueMMapFileModel.readContent(consumeQueueOffset, batchSize);
        List<ConsumeMsgCommitLogDTO> commitLogBodyContentList = new ArrayList<>();
        for (byte[] content : consumeQueueContentList) {
            ConsumeQueueDetailModel consumeQueueDetailModel = new ConsumeQueueDetailModel();
            consumeQueueDetailModel.buildFromBytes(content);
            CommitLogMMapFileModel commitLogMMapFileModel = CommonCache.getCommitLogMMapFileModelManager().get(topic);
            ConsumeMsgCommitLogDTO consumeMsgCommitLogDTO = commitLogMMapFileModel.readContent(consumeQueueDetailModel.getMsgIndex(), consumeQueueDetailModel.getMsgLength());
            commitLogBodyContentList.add(consumeMsgCommitLogDTO);

        }
        return commitLogBodyContentList;
    }

    /**
     * 更新consumeQueue-offset的值
     *
     * @return
     */
    public boolean ack(String topic, String consumeGroup, Integer queueId) {
        try {
            ConsumeQueueOffsetModel.OffsetTable offsetTable = CommonCache.getConsumeQueueOffsetModel().getOffsetTable();
            Map<String, ConsumeQueueOffsetModel.ConsumerGroupDetail> consumerGroupDetailMap = offsetTable.getTopicConsumerGroupDetail();
            ConsumeQueueOffsetModel.ConsumerGroupDetail consumerGroupDetail = consumerGroupDetailMap.get(topic);
            Map<String, String> consumeQueueOffsetDetailMap = consumerGroupDetail.getConsumerGroupDetailMap().get(consumeGroup);
            String offsetStrInfo = consumeQueueOffsetDetailMap.get(String.valueOf(queueId));
            String[] offsetStrArr = offsetStrInfo.split("#");
            String fileName = offsetStrArr[0];
            Integer currentOffset = Integer.valueOf(offsetStrArr[1]);
            currentOffset += 12;
            consumeQueueOffsetDetailMap.put(String.valueOf(queueId), fileName + "#" + currentOffset);
        } catch (Exception e) {
            System.err.println("ack操作异常");
            e.printStackTrace();
        } finally {
        }
        return true;
    }
}
