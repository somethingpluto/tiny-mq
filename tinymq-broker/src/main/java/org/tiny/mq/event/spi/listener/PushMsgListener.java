package org.tiny.mq.event.spi.listener;

import com.alibaba.fastjson.JSON;
import org.tiny.mq.cache.CommonCache;
import org.tiny.mq.common.coder.TcpMsg;
import org.tiny.mq.common.dto.MessageDTO;
import org.tiny.mq.common.dto.SendMessageToBrokerResponseDTO;
import org.tiny.mq.common.enums.BrokerResponseCode;
import org.tiny.mq.common.enums.MessageSendWay;
import org.tiny.mq.common.enums.SendMessageToBrokerResponseStatus;
import org.tiny.mq.common.event.Listener;
import org.tiny.mq.event.model.PushMsgEvent;


public class PushMsgListener implements Listener<PushMsgEvent> {

    @Override
    public void onReceive(PushMsgEvent event) throws Exception {
        //消息写入commitLog
        MessageDTO messageDTO = event.getMessageDTO();
        CommonCache.getCommitLogAppendHandler().appendMsg(messageDTO);
        int sendWay = messageDTO.getSendWay();
        if (MessageSendWay.ASYNC.getCode() == sendWay) {
            return;
        }
        SendMessageToBrokerResponseDTO sendMessageToBrokerResponseDTO = new SendMessageToBrokerResponseDTO();
        sendMessageToBrokerResponseDTO.setStatus(SendMessageToBrokerResponseStatus.SUCCESS.getCode());
        sendMessageToBrokerResponseDTO.setMsgId(messageDTO.getMsgId());
        TcpMsg responseMsg = new TcpMsg(BrokerResponseCode.SEND_MSG_RESP.getCode(), JSON.toJSONBytes(sendMessageToBrokerResponseDTO));
        event.getChannelHandlerContext().writeAndFlush(responseMsg);
    }
}
