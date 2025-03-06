package org.tiny.mq.netty.nameserver;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.cache.CommonCache;
import org.tiny.mq.common.coder.TcpMsg;
import org.tiny.mq.common.enums.NameServerResponseCode;


@ChannelHandler.Sharable
public class NameServerRespChannelHandler extends SimpleChannelInboundHandler {

    private final Logger logger = LoggerFactory.getLogger(NameServerRespChannelHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
        TcpMsg tcpMsg = (TcpMsg) msg;
        if (NameServerResponseCode.REGISTRY_SUCCESS.getCode() == tcpMsg.getCode()) {
            //注册成功！
            //开启一个定时任务，上报心跳数据给到nameserver
            logger.info("注册成功，开启心跳任务");
            CommonCache.getHeartBeatTaskManager().startTask();
        } else if (NameServerResponseCode.ERROR_USER_OR_PASSWORD.getCode() == tcpMsg.getCode()) {
            //验证失败，抛出异常
            throw new RuntimeException("error nameserver user or password");
        } else if (NameServerResponseCode.HEART_BEAT_SUCCESS.getCode() == tcpMsg.getCode()) {
            logger.info("收到nameserver心跳回应ack");
        }
    }
}
