package org.tiny.mq.event.spi.listener;

import com.alibaba.fastjson2.JSON;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.cache.CommonCache;
import org.tiny.mq.common.coder.TcpMsg;
import org.tiny.mq.common.dto.ConsumeMsgRetryReqDTO;
import org.tiny.mq.common.dto.ConsumeMsgRetryRespDTO;
import org.tiny.mq.common.dto.MessageDTO;
import org.tiny.mq.common.dto.MessageRetryDTO;
import org.tiny.mq.common.enums.AckStatus;
import org.tiny.mq.common.enums.BrokerResponseCode;
import org.tiny.mq.common.event.Listener;
import org.tiny.mq.event.model.ConsumeMsgRetryEvent;
import org.tiny.mq.model.EagleMqTopicModel;
import org.tiny.mq.rebalance.ConsumerInstance;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 处理消费不成功的信息
 */
public class ConsumeMsgRetryListener implements Listener<ConsumeMsgRetryEvent> {
    private static final Logger logger = LoggerFactory.getLogger(ConsumeMsgRetryListener.class);
    private static final List<Integer> RETRY_STEP = Arrays.asList(3, 5, 10, 15, 30);

    @Override
    public void onReceive(ConsumeMsgRetryEvent event) throws Exception {
        logger.info("consume msg retry handler,event:{}", JSON.toJSONString(event));
        ConsumeMsgRetryRespDTO consumeMsgRetryRespDTO = new ConsumeMsgRetryRespDTO();
        consumeMsgRetryRespDTO.setMsgId(event.getMsgId());
        // 获取Retry信息
        ConsumeMsgRetryReqDTO consumeMsgRetryReqDTO = event.getConsumeMsgRetryReqDTO();
        String topic = consumeMsgRetryReqDTO.getTopic();
        InetSocketAddress inetSocketAddress = (InetSocketAddress) event.getChannelHandlerContext().channel().remoteAddress();
        String ip = inetSocketAddress.getHostString();
        int port = inetSocketAddress.getPort();
        consumeMsgRetryReqDTO.setPort(port);
        consumeMsgRetryReqDTO.setIp(ip);
        // 获取queueId
        Integer queueId = consumeMsgRetryReqDTO.getQueueId();
        Integer ackCount = consumeMsgRetryReqDTO.getAckCount();
        String consumeGroup = consumeMsgRetryReqDTO.getConsumerGroup();

        // 查看对应的主题是否存在
        EagleMqTopicModel eagleMqTopicModel = CommonCache.getEagleMqTopicModelMap().get(topic);
        if (eagleMqTopicModel == null) { // 消息对应的topic不存在 ack失败
            consumeMsgRetryRespDTO.setAckStatus(AckStatus.FAIL.getCode());
            event.getChannelHandlerContext().writeAndFlush(new TcpMsg(BrokerResponseCode.CONSUME_MSG_RETRY_RESP.getCode(), JSON.toJSONBytes(consumeMsgRetryRespDTO)));
            return;
        }
        // 查看对应的主题是否有消费者
        Map<String, List<ConsumerInstance>> consumerInstanceMap = CommonCache.getConsumeHoldMap().get(topic);
        if (consumerInstanceMap == null || consumerInstanceMap.isEmpty()) {
            consumeMsgRetryRespDTO.setAckStatus(AckStatus.FAIL.getCode());
            event.getChannelHandlerContext().writeAndFlush(new TcpMsg(BrokerResponseCode.CONSUME_MSG_RETRY_RESP.getCode(), JSON.toJSONBytes(consumeMsgRetryRespDTO)));
            return;
        }
        // 查看投递Retry消息的消费者是否还存在在消费组
        List<ConsumerInstance> consumerInstances = consumerInstanceMap.get(consumeGroup);
        if (CollectionUtils.isEmpty(consumerInstances)) {
            consumeMsgRetryRespDTO.setAckStatus(AckStatus.FAIL.getCode());
            event.getChannelHandlerContext().writeAndFlush(new TcpMsg(BrokerResponseCode.CONSUME_MSG_RETRY_RESP.getCode(), JSON.toJSONBytes(consumeMsgRetryRespDTO)));
            return;
        }
        String currentConsumeReqId = consumeMsgRetryReqDTO.getIp() + ":" + consumeMsgRetryReqDTO.getPort();
        ConsumerInstance matchConsumerInstance = consumerInstances.stream().filter(item -> item.getConsumerReqId().equals(currentConsumeReqId)).findAny().orElse(null);
        if (matchConsumerInstance == null) {
            consumeMsgRetryRespDTO.setAckStatus(AckStatus.FAIL.getCode());
            event.getChannelHandlerContext().writeAndFlush(new TcpMsg(BrokerResponseCode.CONSUME_MSG_RETRY_RESP.getCode(), JSON.toJSONBytes(consumeMsgRetryRespDTO)));
            return;
        }
        // 先保障后序的数据内容可以继续处理 避免出现消息大量堆积
        for (int i = 0; i < ackCount; i++) {
            CommonCache.getConsumeQueueConsumeHandler().ack(topic, consumeGroup, queueId);
        }
        List<Long> commitLogOffsetList = consumeMsgRetryReqDTO.getCommitLogOffsetList();
        List<Integer> consumeQueueOffsetList = consumeMsgRetryReqDTO.getCommitLogMsgLengthList();
        for (int i = 0; i < commitLogOffsetList.size(); i++) {
            Long commitLogMsgLength = commitLogOffsetList.get(i);
            Integer commitLogOffset = consumeQueueOffsetList.get(i);
            MessageRetryDTO messageRetryDTO = new MessageRetryDTO();
            messageRetryDTO.setTopic("retry");
            messageRetryDTO.setQueueId(consumeMsgRetryReqDTO.getQueueId());
            messageRetryDTO.setConsumeGroup(consumeMsgRetryReqDTO.getConsumerGroup());
            messageRetryDTO.setSourceCommitLogOffset((int) commitLogOffset);
            messageRetryDTO.setSourceCommitLogSize(commitLogMsgLength);
            messageRetryDTO.setRetryCount(consumeMsgRetryReqDTO.getRetryTime());
            // rocketmq 里会有一个重试的间隔数组
            Integer nextRetryTimeStep = RETRY_STEP.get(consumeMsgRetryReqDTO.getRetryTime());
            if (nextRetryTimeStep == null) { // 重试完了还是不行
                // TODO:丢到死信队列当中
            } else {
                long nextRetryTime = System.currentTimeMillis() + nextRetryTimeStep * 1000;
                messageRetryDTO.setNextRetryTime(nextRetryTime);
            }
            MessageDTO messageDTO = new MessageDTO();
            messageDTO.setTopic("retry");
            messageDTO.setMsgId(event.getMsgId());
            // 塞入重试信息的dto对象的字节数组
            messageDTO.setBody(JSON.toJSONBytes(messageRetryDTO));
            messageDTO.setRetry(true);
            CommonCache.getCommitLogAppendHandler().appendMsg(messageDTO);
            // TODO: 往时间轮组件投送消息
            // topic->面对非常多的消费者 消息重试->往同一个consumerGroup里面投递，topic投递，consumeQueue投递
            logger.info("retry message content:{}", JSON.toJSONString(messageRetryDTO));
        }

        TcpMsg tcpMsg = new TcpMsg(BrokerResponseCode.CONSUME_MSG_RETRY_RESP.getCode(), JSON.toJSONBytes(consumeMsgRetryRespDTO));
        event.getChannelHandlerContext().writeAndFlush(tcpMsg);
    }
}
