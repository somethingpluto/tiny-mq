package org.tiny.mq.event.spi.listener;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.cache.CommonCache;
import org.tiny.mq.common.coder.TcpMsg;
import org.tiny.mq.common.dto.MessageDTO;
import org.tiny.mq.common.dto.SendMessageToBrokerResponseDTO;
import org.tiny.mq.common.enums.BrokerResponseCode;
import org.tiny.mq.common.enums.SendMessageToBrokerResponseStatus;
import org.tiny.mq.common.event.Listener;
import org.tiny.mq.common.utils.AssertUtils;
import org.tiny.mq.event.model.PushMsgEvent;
import org.tiny.mq.timewheel.DelayMessageDTO;
import org.tiny.mq.timewheel.SlotStoreTypeEnum;


public class PushMsgListener implements Listener<PushMsgEvent> {
    private static final Logger logger = LoggerFactory.getLogger(PushMsgListener.class);

    @Override
    public void onReceive(PushMsgEvent event) throws Exception {
        logger.info("push msg handler,event:{}", JSON.toJSONString(event));
        MessageDTO messageDTO = event.getMessageDTO();
        int delay = messageDTO.getDelay();
        if (delay != 0) {
            this.appendDelayMsg(event);
        } else {
            CommonCache.getCommitLogAppendHandler().appendMsg(event);
        }
    }

    private void appendDelayMsg(PushMsgEvent event) {
        MessageDTO messageDTO = event.getMessageDTO();
        int delay = messageDTO.getDelay();
        AssertUtils.isTrue(delay <= 3600, "too large delay second");
        DelayMessageDTO delayMessageDTO = new DelayMessageDTO();
        delayMessageDTO.setData(messageDTO);
        delayMessageDTO.setSlotStoreTypeEnum(SlotStoreTypeEnum.DELAY_MESSAGE_DTO);
        delayMessageDTO.setDelay(messageDTO.getDelay());
        CommonCache.getTimeWheelModelManager().add(delayMessageDTO);
        SendMessageToBrokerResponseDTO sendMessageToBrokerResponseDTO = new SendMessageToBrokerResponseDTO();
        sendMessageToBrokerResponseDTO.setMsgId(messageDTO.getMsgId());
        sendMessageToBrokerResponseDTO.setStatus(SendMessageToBrokerResponseStatus.SUCCESS.getCode());
        TcpMsg tcpMsg = new TcpMsg(BrokerResponseCode.SEND_MSG_RESP.getCode(), JSON.toJSONBytes(sendMessageToBrokerResponseDTO));
        event.getChannelHandlerContext().writeAndFlush(tcpMsg);
    }

}
