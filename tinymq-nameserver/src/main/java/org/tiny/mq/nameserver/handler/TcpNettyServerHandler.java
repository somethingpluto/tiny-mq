package org.tiny.mq.nameserver.handler;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.common.codec.TcpMessage;
import org.tiny.mq.common.dto.ServiceRegistryReqDTO;
import org.tiny.mq.common.enums.NameServerEventCode;
import org.tiny.mq.common.enums.NameServerResponseCode;
import org.tiny.mq.nameserver.eventbus.EventBus;
import org.tiny.mq.nameserver.eventbus.event.Event;
import org.tiny.mq.nameserver.eventbus.event.HeartBeatEvent;
import org.tiny.mq.nameserver.eventbus.event.RegistryEvent;
import org.tiny.mq.nameserver.eventbus.event.UnRegistryEvent;

import java.net.InetSocketAddress;


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
            event = handleRegistryEvent(body, channelHandlerContext);
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


    public RegistryEvent handleRegistryEvent(byte[] body, ChannelHandlerContext channelHandlerContext) {
        ServiceRegistryReqDTO serviceRegistryReqDTO = JSON.parseObject(body, ServiceRegistryReqDTO.class);
        RegistryEvent registryEvent = new RegistryEvent();
        registryEvent.setMsgId(serviceRegistryReqDTO.getMsgId());
        registryEvent.setUser(serviceRegistryReqDTO.getUser());
        registryEvent.setPassword(serviceRegistryReqDTO.getPassword());
        if (StringUtil.isNullOrEmpty(serviceRegistryReqDTO.getIp())) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress) channelHandlerContext.channel().remoteAddress();
            registryEvent.setPort(inetSocketAddress.getPort());
            registryEvent.setIp(inetSocketAddress.getHostString());
        } else {
            registryEvent.setPort(serviceRegistryReqDTO.getPort());
            registryEvent.setIp(serviceRegistryReqDTO.getIp());
        }
        registryEvent.setRegistryType(serviceRegistryReqDTO.getRegistryType());
        logger.info("[EVENT][REGISTRY] from broker {}", registryEvent);
        return registryEvent;
    }

    public HeartBeatEvent handleHeartBeatEvent(byte[] body) {
        HeartBeatEvent event = JSON.parseObject(body, HeartBeatEvent.class);
        return event;
    }

    public UnRegistryEvent handleUnRegistryEvent(byte[] body) {
        UnRegistryEvent event = JSON.parseObject(body, UnRegistryEvent.class);
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
        InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        unRegistryEvent.setIp(socketAddress.getHostString());
        unRegistryEvent.setPort(socketAddress.getPort());
        unRegistryEvent.setChannelHandlerContext(ctx);
        eventBus.publish(unRegistryEvent);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
