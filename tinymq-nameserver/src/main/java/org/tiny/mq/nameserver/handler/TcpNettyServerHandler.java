package org.tiny.mq.nameserver.handler;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.internal.StringUtil;
import org.tiny.mq.common.coder.TcpMsg;
import org.tiny.mq.common.dto.HeartBeatDTO;
import org.tiny.mq.common.dto.PullBrokerIpReqDTO;
import org.tiny.mq.common.dto.ServiceRegistryReqDTO;
import org.tiny.mq.common.enums.NameServerEventCode;
import org.tiny.mq.common.event.EventBus;
import org.tiny.mq.common.event.model.Event;
import org.tiny.mq.nameserver.event.model.HeartBeatEvent;
import org.tiny.mq.nameserver.event.model.PullBrokerIpEvent;
import org.tiny.mq.nameserver.event.model.RegistryEvent;
import org.tiny.mq.nameserver.event.model.UnRegistryEvent;

import java.net.InetSocketAddress;


@ChannelHandler.Sharable
public class TcpNettyServerHandler extends SimpleChannelInboundHandler {
    private EventBus eventBus;

    public TcpNettyServerHandler(EventBus eventBus) {
        this.eventBus = eventBus;
        this.eventBus.init();
    }

    //1.网络请求的接收(netty完成)
    //2.事件发布器的实现（EventBus-》event）Spring的事件，Google Guaua
    //3.事件处理器的实现（Listener-》处理event）
    //4.数据存储（基于Map本地内存的方式存储）
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
        TcpMsg tcpMsg = (TcpMsg) msg;
        int code = tcpMsg.getCode();
        byte[] body = tcpMsg.getBody();
        Event event = null;
        if (NameServerEventCode.REGISTRY.getCode() == code) {
            event = handleRegistry(body, channelHandlerContext);
        } else if (NameServerEventCode.HEART_BEAT.getCode() == code) {
            event = handleHeartBeat(body, channelHandlerContext);
        } else if (NameServerEventCode.PULL_BROKER_IP_LIST.getCode() == code) {
            event = handlePullBrokerIPList(body, channelHandlerContext);
        }
        eventBus.publish(event);
    }

    private Event handleRegistry(byte[] body, ChannelHandlerContext channelHandlerContext) {
        ServiceRegistryReqDTO serviceRegistryReqDTO = JSON.parseObject(body, ServiceRegistryReqDTO.class);
        RegistryEvent registryEvent = new RegistryEvent();
        registryEvent.setMsgId(serviceRegistryReqDTO.getMsgId());
        registryEvent.setPassword(serviceRegistryReqDTO.getPassword());
        registryEvent.setUser(serviceRegistryReqDTO.getUser());
        registryEvent.setAttrs(serviceRegistryReqDTO.getAttrs());
        registryEvent.setRegistryType(serviceRegistryReqDTO.getRegistryType());
        registryEvent.setChannelHandlerContext(channelHandlerContext);
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

    private Event handleHeartBeat(byte[] body, ChannelHandlerContext channelHandlerContext) {
        HeartBeatDTO heartBeatDTO = JSON.parseObject(body, HeartBeatDTO.class);
        HeartBeatEvent heartBeatEvent = new HeartBeatEvent();
        heartBeatEvent.setMsgId(heartBeatDTO.getMsgId());
        heartBeatEvent.setChannelHandlerContext(channelHandlerContext);
        return heartBeatEvent;
    }

    private Event handlePullBrokerIPList(byte[] body, ChannelHandlerContext channelHandlerContext) {
        PullBrokerIpReqDTO pullBrokerIpDTO = JSON.parseObject(body, PullBrokerIpReqDTO.class);
        PullBrokerIpEvent pullBrokerIpEvent = new PullBrokerIpEvent();
        pullBrokerIpEvent.setRole(pullBrokerIpDTO.getRole());
        pullBrokerIpEvent.setMsgId(pullBrokerIpDTO.getMsgId());
        pullBrokerIpEvent.setBrokerClusterGroup(pullBrokerIpDTO.getBrokerClusterGroup());
        pullBrokerIpEvent.setChannelHandlerContext(channelHandlerContext);
        return pullBrokerIpEvent;
    }

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
