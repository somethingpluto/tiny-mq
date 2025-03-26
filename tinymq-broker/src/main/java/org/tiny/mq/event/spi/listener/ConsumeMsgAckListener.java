package org.tiny.mq.event.spi.listener;

import com.alibaba.fastjson.JSON;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.cache.CommonCache;
import org.tiny.mq.common.coder.TcpMsg;
import org.tiny.mq.common.dto.ConsumeMsgAckReqDTO;
import org.tiny.mq.common.dto.ConsumeMsgAckRespDTO;
import org.tiny.mq.common.enums.AckStatus;
import org.tiny.mq.common.enums.BrokerResponseCode;
import org.tiny.mq.common.event.Listener;
import org.tiny.mq.model.ConsumeMsgAckEvent;
import org.tiny.mq.model.EagleMqTopicModel;
import org.tiny.mq.rebalance.ConsumerInstance;

import java.util.List;
import java.util.Map;


public class ConsumeMsgAckListener implements Listener<ConsumeMsgAckEvent> {
    private final Logger logger = LoggerFactory.getLogger(ConsumeMsgAckListener.class);

    @Override
    public void onReceive(ConsumeMsgAckEvent event) throws Exception {
        logger.info("consume msg ack handler,event:{}", JSON.toJSONString(event));
        ConsumeMsgAckReqDTO consumeMsgAckReqDTO = event.getConsumeMsgAckReqDTO();
        String topic = consumeMsgAckReqDTO.getTopic();
        String consumeGroup = consumeMsgAckReqDTO.getConsumeGroup();
        Integer queueId = consumeMsgAckReqDTO.getQueueId();
        Integer ackCount = consumeMsgAckReqDTO.getAckCount();
        ConsumeMsgAckRespDTO consumeMsgAckRespDTO = new ConsumeMsgAckRespDTO();
        consumeMsgAckRespDTO.setMsgId(event.getMsgId());
        EagleMqTopicModel eagleMqTopicModel = CommonCache.getEagleMqTopicModelMap().get(topic);
        if (eagleMqTopicModel == null) {
            //topic不存在，ack失败
            consumeMsgAckRespDTO.setAckStatus(AckStatus.FAIL.getCode());
            event.getChannelHandlerContext().writeAndFlush(new TcpMsg(BrokerResponseCode.BROKER_UPDATE_CONSUME_OFFSET_RESP.getCode(), JSON.toJSONBytes(consumeMsgAckRespDTO)));
            return;
        }
        Map<String, List<ConsumerInstance>> consumerInstanceMap = CommonCache.getConsumeHoldMap().get(topic);
        if (consumerInstanceMap == null || consumerInstanceMap.isEmpty()) {
            consumeMsgAckRespDTO.setAckStatus(AckStatus.FAIL.getCode());
            event.getChannelHandlerContext().writeAndFlush(new TcpMsg(BrokerResponseCode.BROKER_UPDATE_CONSUME_OFFSET_RESP.getCode(), JSON.toJSONBytes(consumeMsgAckRespDTO)));
            return;
        }
        List<ConsumerInstance> consumeGroupInstances = consumerInstanceMap.get(consumeGroup);
        if (CollectionUtils.isEmpty(consumeGroupInstances)) {
            consumeMsgAckRespDTO.setAckStatus(AckStatus.FAIL.getCode());
            event.getChannelHandlerContext().writeAndFlush(new TcpMsg(BrokerResponseCode.BROKER_UPDATE_CONSUME_OFFSET_RESP.getCode(),
                    JSON.toJSONBytes(consumeMsgAckRespDTO)));
            return;
        }
        String currentConsumeReqId = consumeMsgAckReqDTO.getIp() + ":" + consumeMsgAckReqDTO.getPort();
        ConsumerInstance matchInstance = consumeGroupInstances.stream().filter(item -> item.getConsumerReqId().equals(currentConsumeReqId)).findAny().orElse(null);
        if (matchInstance == null) {
            consumeMsgAckRespDTO.setAckStatus(AckStatus.FAIL.getCode());
            event.getChannelHandlerContext().writeAndFlush(new TcpMsg(BrokerResponseCode.BROKER_UPDATE_CONSUME_OFFSET_RESP.getCode(),
                    JSON.toJSONBytes(consumeMsgAckRespDTO)));
            return;
        }
        for (int i = 0; i < ackCount; i++) {
            CommonCache.getConsumeQueueConsumeHandler().ack(topic, consumeGroup, queueId);
        }

        // 将消费失败的数据重新推送到一条队列上

        logger.info("broker receive offset value ,topic is {},consumeGroup is {},queueId is {},ackCount is {}",
                topic, consumeGroup, queueId, ackCount);
        consumeMsgAckRespDTO.setAckStatus(AckStatus.SUCCESS.getCode());
        TcpMsg tcpMsg = new TcpMsg(BrokerResponseCode.BROKER_UPDATE_CONSUME_OFFSET_RESP.getCode(),
                JSON.toJSONBytes(consumeMsgAckRespDTO));
        event.getChannelHandlerContext().writeAndFlush(tcpMsg);
    }
}
