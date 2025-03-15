package org.tiny.mq.netty;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import org.tiny.mq.async.model.BrokerConnectionClosedEvent;
import org.tiny.mq.common.cache.BrokerServerSyncFutureManager;
import org.tiny.mq.common.coder.TcpMsg;
import org.tiny.mq.common.dto.ConsumeMsgAckRespDTO;
import org.tiny.mq.common.dto.ConsumeMsgBaseRespDTO;
import org.tiny.mq.common.dto.SendMessageToBrokerResponseDTO;
import org.tiny.mq.common.enums.BrokerResponseCode;
import org.tiny.mq.common.event.EventBus;
import org.tiny.mq.common.remote.SyncFuture;

import java.net.InetSocketAddress;

public class RemoteBrokerRespHandler extends SimpleChannelInboundHandler {

    private EventBus eventBus;

    public RemoteBrokerRespHandler(EventBus eventBus) {
        this.eventBus = eventBus;
        eventBus.init();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        TcpMsg tcpMsg = (TcpMsg) o;
        int code = tcpMsg.getCode();
        byte[] body = tcpMsg.getBody();
        if (code == BrokerResponseCode.SEND_MSG_RESP.getCode()) {
            SendMessageToBrokerResponseDTO sendMessageToBrokerResponseDTO = JSON.parseObject(body, SendMessageToBrokerResponseDTO.class);
            SyncFuture syncFuture = BrokerServerSyncFutureManager.get(sendMessageToBrokerResponseDTO.getMsgId());
            if (syncFuture != null) {
                syncFuture.setResponse(tcpMsg);
            }
        } else if (code == BrokerResponseCode.CONSUME_MSG_RESP.getCode()) {
            ConsumeMsgBaseRespDTO consumeMsgBaseRespDTO = JSON.parseObject(body, ConsumeMsgBaseRespDTO.class);
            SyncFuture syncFuture = BrokerServerSyncFutureManager.get(consumeMsgBaseRespDTO.getMsgId());
            if (syncFuture != null) {
                syncFuture.setResponse(tcpMsg);
            }
        } else if (code == BrokerResponseCode.BROKER_UPDATE_CONSUME_OFFSET_RESP.getCode()) {
            ConsumeMsgAckRespDTO consumeMsgAckRespDTO = JSON.parseObject(body, ConsumeMsgAckRespDTO.class);
            SyncFuture syncFuture = BrokerServerSyncFutureManager.get(consumeMsgAckRespDTO.getMsgId());
            if (syncFuture != null) {
                syncFuture.setResponse(tcpMsg);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress inetSocketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        String reqId = inetSocketAddress.getHostString() + ":" + inetSocketAddress.getPort();
        ctx.attr(AttributeKey.valueOf("reqId")).set(reqId);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        System.out.println("通道关闭");
        //需要触发一个事件出来
        BrokerConnectionClosedEvent brokerConnectionClosedEvent = new BrokerConnectionClosedEvent();
        brokerConnectionClosedEvent.setBrokerReqId((String) ctx.attr(AttributeKey.valueOf("reqId")).get());
        eventBus.publish(brokerConnectionClosedEvent);
    }
}
