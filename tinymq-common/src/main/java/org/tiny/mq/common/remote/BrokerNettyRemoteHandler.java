package org.tiny.mq.common.remote;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.common.cache.BrokerServerSyncFutureManager;
import org.tiny.mq.common.codec.TcpMessage;
import org.tiny.mq.common.dto.ConsumeMsgAckRespDTO;
import org.tiny.mq.common.dto.SendMessageToBrokerRespDTO;
import org.tiny.mq.common.enums.BrokerResponseCode;

public class BrokerNettyRemoteHandler extends SimpleChannelInboundHandler {
    private static final Logger logger = LoggerFactory.getLogger(BrokerNettyRemoteHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {

        TcpMessage tcpMessage = (TcpMessage) o;
        int code = tcpMessage.getCode();
        byte[] body = tcpMessage.getBody();
        if (code == BrokerResponseCode.SEND_MSG_RESP.getCode()) {
            SendMessageToBrokerRespDTO sendMessageToBrokerRespDTO = JSON.parseObject(body, SendMessageToBrokerRespDTO.class);
            logger.info("send message to broker success:{}", sendMessageToBrokerRespDTO);

            SyncFuture syncFuture = BrokerServerSyncFutureManager.get(sendMessageToBrokerRespDTO.getMsgId());
            if (syncFuture != null) {
                syncFuture.setResponse(tcpMessage);
            }
        } else if (code == BrokerResponseCode.CONSUME_MSG_RESP.getCode()) {
            ConsumeMsgAckRespDTO consumeMsgAckRespDTO = JSON.parseObject(body, ConsumeMsgAckRespDTO.class);
            SyncFuture syncFuture = BrokerServerSyncFutureManager.get(consumeMsgAckRespDTO.getMsgId());
            if (syncFuture != null) {
                syncFuture.setResponse(tcpMessage);
            }
        } else if (code == BrokerResponseCode.BROKER_UPDATE_CONSUME_OFFSET_RESP.getCode()) {
            ConsumeMsgAckRespDTO consumeMsgAckRespDTO = JSON.parseObject(body, ConsumeMsgAckRespDTO.class);
            SyncFuture syncFuture = BrokerServerSyncFutureManager.get(consumeMsgAckRespDTO.getMsgId());
            if (syncFuture != null) {
                syncFuture.setResponse(tcpMessage);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        System.out.println("通道关闭");
    }
}
