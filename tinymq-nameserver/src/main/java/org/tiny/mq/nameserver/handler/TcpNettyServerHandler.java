package org.tiny.mq.nameserver.handler;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.common.codec.TcpMessage;
import org.tiny.mq.common.enums.NameServerEventCode;
import org.tiny.mq.common.enums.NameServerResponseCode;
import org.tiny.mq.nameserver.eventbus.EventBus;
import org.tiny.mq.nameserver.eventbus.event.Event;
import org.tiny.mq.nameserver.eventbus.event.HeartBeatEvent;
import org.tiny.mq.nameserver.eventbus.event.RegistryEvent;
import org.tiny.mq.nameserver.eventbus.event.UnRegistryEvent;


/**
 * TCP 连接注册事件处理器
 */
@ChannelHandler.Sharable
public class TcpNettyServerHandler extends SimpleChannelInboundHandler {
    private static final Logger logger = LoggerFactory.getLogger(TcpNettyServerHandler.class);
    private final EventBus eventBus;

    public TcpNettyServerHandler(EventBus eventBus) {
        this.eventBus = eventBus;
    }


    /**
     * accept tcp connect judge type
     *
     * @param channelHandlerContext
     * @param o
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        TcpMessage message = (TcpMessage) o;
        int code = message.getCode();
        byte[] body = message.getBody();
        Event event;
        if (NameServerEventCode.REGISTRY.getCode() == code) {
            event = handleRegistryEvent(body);
        } else if (NameServerEventCode.HEART_BEAT.getCode() == code) {
            event = handleHeartBeatEvent(body);
        } else if (NameServerEventCode.UN_REGISTRY.getCode() == code) {
            event = handleUnRegistryEvent(body);
        } else {
            handleUnknownEvent(channelHandlerContext);
            return;
        }
        event.setChannelHandlerContext(channelHandlerContext);
        eventBus.publish(event);
    }


    public RegistryEvent handleRegistryEvent(byte[] body) {
        RegistryEvent event = JSON.parseObject(body, RegistryEvent.class);
        logger.info("[EVENT][REGISTRY] from broker {}", event.getIPAddr());
        return event;
    }

    public HeartBeatEvent handleHeartBeatEvent(byte[] body) {
        HeartBeatEvent event = JSON.parseObject(body, UnRegistryEvent.class);
        logger.info("[EVENT][HEART BEAT EVENT] from broker {}", event.getIPAddr());
        return event;
    }

    public UnRegistryEvent handleUnRegistryEvent(byte[] body) {
        UnRegistryEvent event = JSON.parseObject(body, UnRegistryEvent.class);
        logger.info("[EVENT][UN_REGISTRY EVENT] from broker {}", event.getIPAddr());
        return event;
    }

    public void handleUnknownEvent(ChannelHandlerContext channelHandlerContext) {
        TcpMessage unknownMessage = new TcpMessage(NameServerResponseCode.UNKNOWN_EVENT.getCode(), NameServerResponseCode.UNKNOWN_EVENT.getDesc().getBytes());
        channelHandlerContext.writeAndFlush(unknownMessage);
        channelHandlerContext.close();
    }

    /**
     * tcp disconnect action
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        UnRegistryEvent unRegistryEvent = new UnRegistryEvent();
        unRegistryEvent.setChannelHandlerContext(ctx);
        eventBus.publish(unRegistryEvent);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
