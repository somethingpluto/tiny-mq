package org.tiny.mq.nameserver.handler;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.common.codec.TcpMessage;
import org.tiny.mq.common.dto.HeartBeatDTO;
import org.tiny.mq.common.dto.PullBrokerIpReqDTO;
import org.tiny.mq.common.dto.ServiceRegistryReqDTO;
import org.tiny.mq.common.enums.NameServerEventCode;
import org.tiny.mq.common.enums.NameServerResponseCode;
import org.tiny.mq.common.eventbus.Event;
import org.tiny.mq.common.eventbus.EventBus;
import org.tiny.mq.nameserver.eventbus.event.HeartBeatEvent;
import org.tiny.mq.nameserver.eventbus.event.PullBrokerIPListEvent;
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
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) {
        TcpMessage message = (TcpMessage) o;
        int code = message.getCode();
        byte[] body = message.getBody();
        Event event;
        if (NameServerEventCode.REGISTRY.getCode() == code) {
            event = handleRegistryEvent(body, channelHandlerContext);
        } else if (NameServerEventCode.HEART_BEAT.getCode() == code) {
            event = handleHeartBeatEvent(body, channelHandlerContext);
        } else if (NameServerEventCode.UN_REGISTRY.getCode() == code) {
            event = handleUnRegistryEvent(body, channelHandlerContext);
        } else if (NameServerEventCode.PULL_BROKER_IP_LIST.getCode() == code) {
            event = handlePullBrokerIPListEvent(body, channelHandlerContext);
        } else {
            handleUnknownEvent(channelHandlerContext);
            return;
        }
        event.setChannelHandlerContext(channelHandlerContext);
        eventBus.publish(event);
    }

    private PullBrokerIPListEvent handlePullBrokerIPListEvent(byte[] body, ChannelHandlerContext channelHandlerContext) {
        PullBrokerIpReqDTO pullBrokerIpReqDTO = JSON.parseObject(body, PullBrokerIpReqDTO.class);
        PullBrokerIPListEvent pullBrokerIPListEvent = new PullBrokerIPListEvent();
        pullBrokerIPListEvent.setRole(pullBrokerIpReqDTO.getRole());
        pullBrokerIPListEvent.setMsgId(pullBrokerIpReqDTO.getMsgId());
        return pullBrokerIPListEvent;
    }


    private RegistryEvent handleRegistryEvent(byte[] body, ChannelHandlerContext channelHandlerContext) {
        ServiceRegistryReqDTO serviceRegistryReqDTO = JSON.parseObject(body, ServiceRegistryReqDTO.class);
        RegistryEvent registryEvent = new RegistryEvent();
        registryEvent.setMsgId(serviceRegistryReqDTO.getMsgId());
        registryEvent.setUser(serviceRegistryReqDTO.getUser());
        registryEvent.setPassword(serviceRegistryReqDTO.getPassword());
        registryEvent.setRegistryType(serviceRegistryReqDTO.getRegistryType());
        if (StringUtil.isNullOrEmpty(serviceRegistryReqDTO.getIp())) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress) channelHandlerContext.channel().remoteAddress();
            registryEvent.setPort(inetSocketAddress.getPort());
            registryEvent.setIp(inetSocketAddress.getHostString());
        } else {
            registryEvent.setPort(serviceRegistryReqDTO.getPort());
            registryEvent.setIp(serviceRegistryReqDTO.getIp());
        }
        return registryEvent;
    }

    private HeartBeatEvent handleHeartBeatEvent(byte[] body, ChannelHandlerContext channelHandlerContext) {
        HeartBeatDTO heartBeatDTO = JSON.parseObject(body, HeartBeatDTO.class);
        HeartBeatEvent heartBeatEvent = new HeartBeatEvent();
        heartBeatEvent.setMsgId(heartBeatDTO.getMsgId());
        return heartBeatEvent;
    }

    private UnRegistryEvent handleUnRegistryEvent(byte[] body, ChannelHandlerContext channelHandlerContext) {
        return JSON.parseObject(body, UnRegistryEvent.class);
    }

    private void handleUnknownEvent(ChannelHandlerContext channelHandlerContext) {
        TcpMessage unknownMessage = new TcpMessage(NameServerResponseCode.UNKNOWN_EVENT.getCode(), NameServerResponseCode.UNKNOWN_EVENT.getDesc().getBytes());
        channelHandlerContext.writeAndFlush(unknownMessage);
        channelHandlerContext.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
