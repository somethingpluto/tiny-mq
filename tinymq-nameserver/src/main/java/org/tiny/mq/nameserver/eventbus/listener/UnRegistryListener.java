package org.tiny.mq.nameserver.eventbus.listener;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.common.codec.TcpMessage;
import org.tiny.mq.common.enums.NameServerResponseCode;
import org.tiny.mq.nameserver.config.GlobalConfig;
import org.tiny.mq.nameserver.eventbus.event.UnRegistryEvent;

/**
 * 登出事件处理器
 */
public class UnRegistryListener implements Listener<UnRegistryEvent> {
    private static final Logger logger = LoggerFactory.getLogger(UnRegistryListener.class);

    @Override
    public void onReceive(UnRegistryEvent event) throws IllegalAccessException {
        ChannelHandlerContext channelHandlerContext = event.getChannelHandlerContext();
        Object reqId = channelHandlerContext.attr(AttributeKey.valueOf("reqId")).get();
        String reqIdentify = (String) reqId;
        logger.info("un registry broker {}", reqIdentify);
        if (reqIdentify == null) {
            TcpMessage message = new TcpMessage(NameServerResponseCode.ERROR_USER_OR_PASSWORD.getCode(), NameServerResponseCode.ERROR_USER_OR_PASSWORD.getDesc().getBytes());
            channelHandlerContext.writeAndFlush(message);
            channelHandlerContext.close();
            throw new IllegalAccessException("error account to connected!");
        }
        GlobalConfig.getServiceInstanceManager().remove(reqIdentify);
        channelHandlerContext.writeAndFlush(new TcpMessage(NameServerResponseCode.UN_REGISTRY_SERVICE.getCode(), NameServerResponseCode.UN_REGISTRY_SERVICE.getDesc().getBytes()));
        channelHandlerContext.close();
    }
}
