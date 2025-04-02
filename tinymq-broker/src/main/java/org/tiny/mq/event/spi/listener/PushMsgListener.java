package org.tiny.mq.event.spi.listener;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.cache.CommonCache;
import org.tiny.mq.common.coder.TcpMsg;
import org.tiny.mq.common.dto.MessageDTO;
import org.tiny.mq.common.dto.SendMessageToBrokerResponseDTO;
import org.tiny.mq.common.dto.TransactionMsgDTO;
import org.tiny.mq.common.enums.BrokerResponseCode;
import org.tiny.mq.common.enums.SendMessageToBrokerResponseStatus;
import org.tiny.mq.common.enums.TransactionMessageFlagEnum;
import org.tiny.mq.common.event.Listener;
import org.tiny.mq.common.event.model.Event;
import org.tiny.mq.common.utils.AssertUtils;
import org.tiny.mq.event.model.PushMsgEvent;
import org.tiny.mq.model.TransactionMsgAckModel;
import org.tiny.mq.timewheel.DelayMessageDTO;
import org.tiny.mq.timewheel.SlotStoreTypeEnum;


public class PushMsgListener implements Listener<PushMsgEvent> {
    private static final Logger logger = LoggerFactory.getLogger(PushMsgListener.class);

    @Override
    public void onReceive(PushMsgEvent event) throws Exception {
        logger.info("push msg handler,event:{}", JSON.toJSONString(event));
        MessageDTO messageDTO = event.getMessageDTO();
        boolean isDelayMsg = messageDTO.getDelay() > 0;
        boolean isHalfMsg = messageDTO.getTxFlag() == TransactionMessageFlagEnum.REMAIN_HALF_FLAG.getCode();
        boolean isTxMsgRemainHalfAck = messageDTO.getTxFlag() == TransactionMessageFlagEnum.REMAIN_HALF_FLAG.getCode();
        if (isDelayMsg) {
            this.appendDelayMsg(messageDTO, event);
        } else if (isHalfMsg) {

        } else if (isTxMsgRemainHalfAck) {

        } else {
            this.appendDefaultMsg(messageDTO, event);
        }
    }

    private void handleHalfMsg(MessageDTO messageDTO, Event event) {
        TransactionMsgAckModel transactionMsgAckModel = new TransactionMsgAckModel();
        transactionMsgAckModel.setMsgId(messageDTO.getMsgId());
        transactionMsgAckModel.setChannelHandlerContext(event.getChannelHandlerContext());
        transactionMsgAckModel.setFirstSendTime(System.currentTimeMillis());
        CommonCache.getTransactionMsgModelMap().put(messageDTO.getMsgId(), transactionMsgAckModel);
        // 时间轮推送
        TransactionMsgDTO transactionMsgDTO = new TransactionMsgDTO();
        transactionMsgDTO.setMsgId(messageDTO.getMsgId());
        long currentTime = System.currentTimeMillis();
        DelayMessageDTO delayMessageDTO = new DelayMessageDTO();
        delayMessageDTO.setData(transactionMsgDTO);
        delayMessageDTO.setNextExecuteTime(currentTime + 3 * 1000);
        delayMessageDTO.setDelay(3);
        delayMessageDTO.setSlotStoreTypeEnum(SlotStoreTypeEnum.TRANSACTION_MSG_DTO);
        CommonCache.getTimeWheelModelManager().add(delayMessageDTO);

    }

    private void appendDefaultMsg(MessageDTO messageDTO, Event event) {
        CommonCache.getCommitLogAppendHandler().appendMsg(messageDTO, event);
    }

    private void appendDelayMsg(MessageDTO messageDTO, Event event) {
        int delay = messageDTO.getDelay();
        AssertUtils.isTrue(delay <= 3600, "too large delay second");
        DelayMessageDTO delayMessageDTO = new DelayMessageDTO();
        delayMessageDTO.setData(messageDTO);
        delayMessageDTO.setSlotStoreTypeEnum(SlotStoreTypeEnum.DELAY_MESSAGE_DTO);
        delayMessageDTO.setDelay(messageDTO.getDelay());
        delayMessageDTO.setNextExecuteTime(System.currentTimeMillis() + delay * 1000L);
        CommonCache.getTimeWheelModelManager().add(delayMessageDTO);
        SendMessageToBrokerResponseDTO sendMessageToBrokerResponseDTO = new SendMessageToBrokerResponseDTO();
        sendMessageToBrokerResponseDTO.setMsgId(messageDTO.getMsgId());
        sendMessageToBrokerResponseDTO.setStatus(SendMessageToBrokerResponseStatus.SUCCESS.getCode());
        TcpMsg tcpMsg = new TcpMsg(BrokerResponseCode.SEND_MSG_RESP.getCode(), JSON.toJSONBytes(sendMessageToBrokerResponseDTO));
        event.getChannelHandlerContext().writeAndFlush(tcpMsg);
    }

}
