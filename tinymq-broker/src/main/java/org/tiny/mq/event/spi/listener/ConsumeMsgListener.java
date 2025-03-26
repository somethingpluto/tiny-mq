package org.tiny.mq.event.spi.listener;

import com.alibaba.fastjson.JSON;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.cache.CommonCache;
import org.tiny.mq.common.coder.TcpMsg;
import org.tiny.mq.common.dto.ConsumeMsgBaseRespDTO;
import org.tiny.mq.common.dto.ConsumeMsgCommitLogDTO;
import org.tiny.mq.common.dto.ConsumeMsgReqDTO;
import org.tiny.mq.common.dto.ConsumeMsgRespDTO;
import org.tiny.mq.common.enums.BrokerResponseCode;
import org.tiny.mq.common.event.Listener;
import org.tiny.mq.event.model.ConsumeMsgEvent;
import org.tiny.mq.model.ConsumeQueueConsumeReqModel;
import org.tiny.mq.rebalance.ConsumerInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ConsumeMsgListener implements Listener<ConsumeMsgEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ConsumeMsgListener.class);

    @Override
    public void onReceive(ConsumeMsgEvent event) throws Exception {
        logger.info("consume msg handler,event:{}", JSON.toJSONString(event));
        ConsumeMsgReqDTO consumeMsgReqDTO = event.getConsumeMsgReqDTO();
        String currentReqId = consumeMsgReqDTO.getIp() + ":" + consumeMsgReqDTO.getPort();
        String topic = consumeMsgReqDTO.getTopic();
        ConsumerInstance consumerInstance = new ConsumerInstance();
        consumerInstance.setIp(consumeMsgReqDTO.getIp());
        consumerInstance.setPort(consumeMsgReqDTO.getPort());
        consumerInstance.setConsumerReqId(currentReqId);
        consumerInstance.setTopic(consumeMsgReqDTO.getTopic());
        consumerInstance.setConsumeGroup(consumeMsgReqDTO.getConsumeGroup());
        consumerInstance.setBatchSize(consumeMsgReqDTO.getBatchSize());
        //加入到消费池中
        CommonCache.getConsumerInstancePool().addInstancePool(consumerInstance);
        // 构建返回消息DTO
        ConsumeMsgBaseRespDTO consumeMsgBaseRespDTO = new ConsumeMsgBaseRespDTO();
        List<ConsumeMsgRespDTO> consumeMsgRespDTOS = new ArrayList<>();
        consumeMsgBaseRespDTO.setConsumeMsgRespDTOList(consumeMsgRespDTOS);
        consumeMsgBaseRespDTO.setMsgId(event.getMsgId());
        Map<String, List<ConsumerInstance>> consumeGroupMap = CommonCache.getConsumeHoldMap().get(topic);
        //有可能当前消费组还没经过第一轮重平衡，因此不会那么快消费到数据,所以要通知客户端，目前服务端还没将队列分配好
        if (consumeGroupMap == null) {
            //直接返回空数据
            event.getChannelHandlerContext().writeAndFlush(new TcpMsg(BrokerResponseCode.CONSUME_MSG_RESP.getCode(), JSON.toJSONBytes(consumeMsgBaseRespDTO)));
            return;
        }
        List<ConsumerInstance> consumerInstances = consumeGroupMap.get(consumeMsgReqDTO.getConsumeGroup());
        if (CollectionUtils.isEmpty(consumerInstances)) {
            //直接返回空数据
            event.getChannelHandlerContext().writeAndFlush(new TcpMsg(BrokerResponseCode.CONSUME_MSG_RESP.getCode(), JSON.toJSONBytes(consumeMsgBaseRespDTO)));
            return;
        }
        for (ConsumerInstance instance : consumerInstances) {
            if (instance.getConsumerReqId().equals(currentReqId)) {
                //当前消费者有占有队列的权利,可以消费 todo
                for (Integer queueId : instance.getQueueIdSet()) {
                    ConsumeQueueConsumeReqModel consumeQueueConsumeReqModel = new ConsumeQueueConsumeReqModel();
                    consumeQueueConsumeReqModel.setTopic(topic);
                    consumeQueueConsumeReqModel.setQueueId(queueId);
                    consumeQueueConsumeReqModel.setBatchSize(instance.getBatchSize());
                    consumeQueueConsumeReqModel.setConsumeGroup(instance.getConsumeGroup());
                    consumeQueueConsumeReqModel.setBatchSize(instance.getBatchSize());
                    List<ConsumeMsgCommitLogDTO> commitLogContentList = CommonCache.getConsumeQueueConsumeHandler().consume(consumeQueueConsumeReqModel);
                    ConsumeMsgRespDTO consumeMsgRespDTO = new ConsumeMsgRespDTO();
                    consumeMsgRespDTO.setQueueId(queueId);
                    consumeMsgRespDTO.setCommitLogContentList(commitLogContentList);
                    consumeMsgRespDTOS.add(consumeMsgRespDTO);
                }
            }
        }
        //直接返回空数据
        byte[] body = JSON.toJSONBytes(consumeMsgBaseRespDTO);
        TcpMsg respMsg = new TcpMsg(BrokerResponseCode.CONSUME_MSG_RESP.getCode(), body);
        event.getChannelHandlerContext().writeAndFlush(respMsg);
    }
}
