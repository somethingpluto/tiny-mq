package org.tiny.mq.eventbus.listener;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.common.codec.TcpMessage;
import org.tiny.mq.common.dto.MessageDTO;
import org.tiny.mq.common.dto.SendMessageToBrokerRespDTO;
import org.tiny.mq.common.enums.BrokerResponseCode;
import org.tiny.mq.common.enums.MessageSendWayEnum;
import org.tiny.mq.common.enums.SendMessageToBrokerRespStatusEnum;
import org.tiny.mq.common.eventbus.Listener;
import org.tiny.mq.config.GlobalCache;
import org.tiny.mq.eventbus.event.PushMessageEvent;

public class PushMessageListener implements Listener<PushMessageEvent> {
    private static final Logger logger = LoggerFactory.getLogger(PushMessageListener.class);

    @Override
    public void onReceive(PushMessageEvent event) throws IllegalAccessException {
        logger.info("[EVENT][Push Message]:{}", event);

        MessageDTO messageDTO = event.getMessageDTO();
        GlobalCache.getCommitLogAppenderHandler().appendMessage(messageDTO);
        int sendWay = messageDTO.getSendWay();
        if (MessageSendWayEnum.ASYNC.getCode() == sendWay) {
            return;
        }
        SendMessageToBrokerRespDTO sendMessageToBrokerRespDTO = new SendMessageToBrokerRespDTO();
        sendMessageToBrokerRespDTO.setStatus(SendMessageToBrokerRespStatusEnum.SUCCESS.getCode());
        sendMessageToBrokerRespDTO.setMsgId(messageDTO.getMsgId());
        TcpMessage tcpMessage = new TcpMessage(BrokerResponseCode.SEND_MSG_RESP.getCode(), JSON.toJSONBytes(sendMessageToBrokerRespDTO));
        event.getChannelHandlerContext().writeAndFlush(tcpMessage);
    }
}
