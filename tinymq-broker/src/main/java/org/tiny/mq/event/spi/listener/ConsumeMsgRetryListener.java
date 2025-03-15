package org.tiny.mq.event.spi.listener;

import com.alibaba.fastjson2.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.common.coder.TcpMsg;
import org.tiny.mq.common.dto.ConsumeMsgRetryReqDTO;
import org.tiny.mq.common.dto.ConsumeMsgRetryRespDTO;
import org.tiny.mq.common.enums.BrokerResponseCode;
import org.tiny.mq.common.event.Listener;
import org.tiny.mq.event.model.ConsumeMsgRetryEvent;

public class ConsumeMsgRetryListener implements Listener<ConsumeMsgRetryEvent> {
    private static final Logger logger = LoggerFactory.getLogger(ConsumeMsgRetryListener.class);

    @Override
    public void onReceive(ConsumeMsgRetryEvent event) throws Exception {
        ConsumeMsgRetryReqDTO consumeMsgLaterReqDTO = event.getConsumeMsgLaterReqDTO();
        logger.info("consume msg retry handler");
        ConsumeMsgRetryRespDTO consumeMsgRetryRespDTO = new ConsumeMsgRetryRespDTO();
        TcpMsg tcpMsg = new TcpMsg(BrokerResponseCode.CONSUME_MSG_RETRY_RESP.getCode(), JSON.toJSONBytes(consumeMsgRetryRespDTO));
        event.getChannelHandlerContext().writeAndFlush(tcpMsg);
    }
}
