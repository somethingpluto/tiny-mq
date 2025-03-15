package org.tiny.mq.netty.broker;

import com.alibaba.fastjson2.JSON;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.cache.CommonCache;
import org.tiny.mq.common.cache.BrokerServerSyncFutureManager;
import org.tiny.mq.common.coder.TcpMsg;
import org.tiny.mq.common.dto.*;
import org.tiny.mq.common.enums.BrokerEventCode;
import org.tiny.mq.common.enums.BrokerResponseCode;
import org.tiny.mq.common.event.EventBus;
import org.tiny.mq.common.event.model.Event;
import org.tiny.mq.common.remote.SyncFuture;
import org.tiny.mq.event.model.*;
import org.tiny.mq.model.ConsumeMsgAckEvent;

import java.net.InetSocketAddress;


@ChannelHandler.Sharable
public class BrokerServerHandler extends SimpleChannelInboundHandler {

    private static final Logger logger = LoggerFactory.getLogger(BrokerServerHandler.class);

    private EventBus eventBus;

    public BrokerServerHandler(EventBus eventBus) {
        this.eventBus = eventBus;
        this.eventBus.init();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
        TcpMsg tcpMsg = (TcpMsg) msg;
        int code = tcpMsg.getCode();
        byte[] body = tcpMsg.getBody();
        Event event = null;
        if (BrokerEventCode.PUSH_MSG.getCode() == code) {
            event = handlePushMsgEvent(body, channelHandlerContext);
        } else if (BrokerEventCode.CONSUME_MSG.getCode() == code) {
            event = handleConsumeMsgEvent(body, channelHandlerContext);
        } else if (BrokerEventCode.CONSUME_SUCCESS_MSG.getCode() == code) {
            event = handleConsumeSuccessMsg(body, channelHandlerContext);
        } else if (BrokerEventCode.CREATE_TOPIC.getCode() == code) {
            event = handleCreateTopicEvent(body, channelHandlerContext);
        } else if (BrokerEventCode.START_SYNC_MSG.getCode() == code) {
            event = handleStartSyncMsgEvent(body, channelHandlerContext);
        } else if (BrokerResponseCode.SLAVE_BROKER_ACCEPT_PUSH_MSG_RESP.getCode() == code) {
            SendMessageToBrokerResponseDTO sendMessageToBrokerResponseDTO = JSON.parseObject(body, SendMessageToBrokerResponseDTO.class);
            SyncFuture syncFuture = BrokerServerSyncFutureManager.get(sendMessageToBrokerResponseDTO.getMsgId());
            if (syncFuture != null) {
                syncFuture.setResponse(tcpMsg);
            }
        } else if (BrokerEventCode.CONSUME_LATER.getCode() == code) {
            event = handleConsumeMsgRetry(body, channelHandlerContext);
        }
        if (event != null) {
            eventBus.publish(event);
        }
    }

    private Event handleStartSyncMsgEvent(byte[] body, ChannelHandlerContext channelHandlerContext) {
        StartSyncReqDTO startSyncReqDTO = JSON.parseObject(body, StartSyncReqDTO.class);
        StartSyncEvent startSyncEvent = new StartSyncEvent();
        startSyncEvent.setMsgId(startSyncReqDTO.getMsgId());
        startSyncEvent.setChannelHandlerContext(channelHandlerContext);
        return startSyncEvent;
    }

    private Event handleCreateTopicEvent(byte[] body, ChannelHandlerContext channelHandlerContext) {
        CreateTopicReqDTO createTopicReqDTO = JSON.parseObject(body, CreateTopicReqDTO.class);
        CreateTopicEvent createTopicEvent = new CreateTopicEvent();
        createTopicEvent.setCreateTopicReqDTO(createTopicReqDTO);
        createTopicEvent.setChannelHandlerContext(channelHandlerContext);
        return createTopicEvent;
    }

    private Event handlePushMsgEvent(byte[] body, ChannelHandlerContext channelHandlerContext) {
        MessageDTO messageDTO = JSON.parseObject(body, MessageDTO.class);
        PushMsgEvent pushMsgEvent = new PushMsgEvent();
        pushMsgEvent.setMsgId(messageDTO.getMsgId());
        pushMsgEvent.setMessageDTO(messageDTO);
        pushMsgEvent.setChannelHandlerContext(channelHandlerContext);
        logger.info("收到消息推送内容:{},message is {}", new String(messageDTO.getBody()), JSON.toJSONString(messageDTO));
        return pushMsgEvent;
    }

    private Event handleConsumeMsgEvent(byte[] body, ChannelHandlerContext channelHandlerContext) {
        ConsumeMsgReqDTO consumeMsgReqDTO = JSON.parseObject(body, ConsumeMsgReqDTO.class);
        InetSocketAddress inetSocketAddress = (InetSocketAddress) channelHandlerContext.channel().remoteAddress();
        consumeMsgReqDTO.setIp(inetSocketAddress.getHostString());
        consumeMsgReqDTO.setPort(inetSocketAddress.getPort());
        ConsumeMsgEvent consumeMsgEvent = new ConsumeMsgEvent();
        consumeMsgEvent.setConsumeMsgReqDTO(consumeMsgReqDTO);
        consumeMsgEvent.setMsgId(consumeMsgReqDTO.getMsgId());
        consumeMsgEvent.setChannelHandlerContext(channelHandlerContext);
        channelHandlerContext.attr(AttributeKey.valueOf("consumer-reqId")).set(consumeMsgReqDTO.getIp() + ":" + consumeMsgReqDTO.getPort());
        return consumeMsgEvent;
    }

    private Event handleConsumeSuccessMsg(byte[] body, ChannelHandlerContext channelHandlerContext) {
        ConsumeMsgAckReqDTO consumeMsgAckReqDTO = JSON.parseObject(body, ConsumeMsgAckReqDTO.class);
        InetSocketAddress inetSocketAddress = (InetSocketAddress) channelHandlerContext.channel().remoteAddress();
        consumeMsgAckReqDTO.setIp(inetSocketAddress.getHostString());
        consumeMsgAckReqDTO.setPort(inetSocketAddress.getPort());
        ConsumeMsgAckEvent consumeMsgAckEvent = new ConsumeMsgAckEvent();
        consumeMsgAckEvent.setConsumeMsgAckReqDTO(consumeMsgAckReqDTO);
        consumeMsgAckEvent.setMsgId(consumeMsgAckReqDTO.getMsgId());
        consumeMsgAckEvent.setChannelHandlerContext(channelHandlerContext);
        return consumeMsgAckEvent;
    }

    private Event handleConsumeMsgRetry(byte[] body, ChannelHandlerContext channelHandlerContext) {
        ConsumeMsgRetryReqDTO consumeMsgRetryReqDTO = JSON.parseObject(body, ConsumeMsgRetryReqDTO.class);
        ConsumeMsgRetryEvent consumeMsgRetryEvent = new ConsumeMsgRetryEvent();
        consumeMsgRetryEvent.setMsgId(consumeMsgRetryReqDTO.getMsgId());
        consumeMsgRetryEvent.setConsumeMsgLaterReqDTO(consumeMsgRetryReqDTO);
        consumeMsgRetryEvent.setChannelHandlerContext(channelHandlerContext);
        return consumeMsgRetryEvent;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        logger.error("error is :", cause);
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        //链接断开的时候，从重平衡池中移除
        Object reqId = ctx.attr(AttributeKey.valueOf("consumer-reqId")).get();
        if (reqId == null) {
            return;
        }
        CommonCache.getConsumerInstancePool().removeFromInstancePool(String.valueOf(reqId));
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("new connection build");
    }
}
