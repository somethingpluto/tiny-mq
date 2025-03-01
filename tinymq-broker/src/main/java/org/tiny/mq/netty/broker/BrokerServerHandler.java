package org.tiny.mq.netty.broker;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.common.codec.TcpMessage;
import org.tiny.mq.common.dto.ConsumeMsgAckReqDTO;
import org.tiny.mq.common.dto.ConsumeMsgReqDTO;
import org.tiny.mq.common.dto.MessageDTO;
import org.tiny.mq.common.enums.BrokerEventCode;
import org.tiny.mq.common.eventbus.Event;
import org.tiny.mq.common.eventbus.EventBus;
import org.tiny.mq.eventbus.event.ConsumeMessageAckEvent;
import org.tiny.mq.eventbus.event.ConsumeMessageEvent;
import org.tiny.mq.eventbus.event.PushMessageEvent;

@ChannelHandler.Sharable
public class BrokerServerHandler extends SimpleChannelInboundHandler {
    private static final Logger logger = LoggerFactory.getLogger(BrokerServerHandler.class);
    private EventBus eventBus;

    public BrokerServerHandler(EventBus eventBus) {
        this.eventBus = eventBus;
        this.eventBus.init();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        TcpMessage tcpMessage = (TcpMessage) o;
        int code = tcpMessage.getCode();
        byte[] body = tcpMessage.getBody();
        Event event = null;
        if (code == BrokerEventCode.PUSH_MSG.getCode()) {
            logger.info("accept message from client {}", body);
            event = handlePushMessageEvent(body, channelHandlerContext);
        } else if (code == BrokerEventCode.CONSUME_MSG.getCode()) {
            event = handleConsumeMessageEvent(body, channelHandlerContext);
        } else if (code == BrokerEventCode.CONSUME_SUCCESS_MSG.getCode()) {
            event = handleConsumeMessageSuccessEvent(body, channelHandlerContext);
        }
        event.setChannelHandlerContext(channelHandlerContext);
        eventBus.publish(event);
    }

    private ConsumeMessageAckEvent handleConsumeMessageSuccessEvent(byte[] body, ChannelHandlerContext channelHandlerContext) {
        ConsumeMsgAckReqDTO consumeMsgAckReqDTO = JSON.parseObject(body, ConsumeMsgAckReqDTO.class);
        ConsumeMessageAckEvent consumeMessageAckEvent = new ConsumeMessageAckEvent();
        consumeMessageAckEvent.setConsumeMsgReqDTO(consumeMsgAckReqDTO);
        return consumeMessageAckEvent;
    }

    private ConsumeMessageEvent handleConsumeMessageEvent(byte[] body, ChannelHandlerContext channelHandlerContext) {
        ConsumeMsgReqDTO consumeMsgReqDTO = JSON.parseObject(body, ConsumeMsgReqDTO.class);
        ConsumeMessageEvent consumeMessageEvent = new ConsumeMessageEvent();
        consumeMessageEvent.setConsumeMsgReqDTO(consumeMsgReqDTO);
        return consumeMessageEvent;
    }

    private PushMessageEvent handlePushMessageEvent(byte[] body, ChannelHandlerContext channelHandlerContext) {
        MessageDTO messageDTO = JSON.parseObject(body, MessageDTO.class);
        PushMessageEvent pushMessageEvent = new PushMessageEvent();
        pushMessageEvent.setMessageDTO(messageDTO);
        return pushMessageEvent;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        logger.error("error is :", cause);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("new connection build");
    }
}
