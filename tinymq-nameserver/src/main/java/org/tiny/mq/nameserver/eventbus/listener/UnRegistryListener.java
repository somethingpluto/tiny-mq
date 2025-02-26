package org.tiny.mq.nameserver.eventbus.listener;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.common.codec.TcpMessage;
import org.tiny.mq.common.enums.MessageTypeEnum;
import org.tiny.mq.common.eventbus.Listener;
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
            TcpMessage message = MessageTypeEnum.ERROR_USER_MESSAGE.getTcpMessage();
            channelHandlerContext.writeAndFlush(message);
            channelHandlerContext.close();
            throw new IllegalAccessException("error account to connected!");
        }
        GlobalConfig.getServiceInstanceManager().remove(reqIdentify);
        TcpMessage message = MessageTypeEnum.UN_REGISTRY_MESSAGE.getTcpMessage();
        channelHandlerContext.writeAndFlush(message);
        channelHandlerContext.close();
    }
}
