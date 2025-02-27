package org.tiny.mq.netty.nameserver;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.common.codec.TcpMessage;
import org.tiny.mq.common.enums.NameServerEventCode;
import org.tiny.mq.common.enums.NameServerResponseCode;
import org.tiny.mq.config.GlobalCache;
import org.tiny.mq.netty.nameserver.manager.HeartBeatManager;


@ChannelHandler.Sharable
public class NameServerRespChannelHandler extends SimpleChannelInboundHandler {

    private static final Logger logger = LoggerFactory.getLogger(NameServerRespChannelHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        TcpMessage message = (TcpMessage) o;
        int code = message.getCode();
        if (code == NameServerResponseCode.REGISTRY_SUCCESS.getCode()) {
            logger.info("register success start heartbeat");
            HeartBeatManager heartBeatManager = GlobalCache.getHeartBeatManager();
            heartBeatManager.startTask();
        } else if (code == NameServerResponseCode.ERROR_USER_OR_PASSWORD.getCode()) {
            // 登录失败
            throw new RuntimeException("error nameserver user or password");
        } else if (code == NameServerResponseCode.HEART_BEAT_SUCCESS.getCode()) {
            logger.info("broker nameserve heartbeat success");
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        TcpMessage message = new TcpMessage(NameServerEventCode.UN_REGISTRY.getCode(), NameServerEventCode.UN_REGISTRY.getDesc().getBytes());
        ctx.writeAndFlush(message);
        logger.info("un registry event");
    }
}
