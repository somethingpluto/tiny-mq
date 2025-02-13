package org.tiny.mq.nameserver.eventbus.listener;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.common.codec.TcpMessage;
import org.tiny.mq.common.enums.MessageTypeEnum;
import org.tiny.mq.nameserver.config.GlobalConfig;
import org.tiny.mq.nameserver.eventbus.event.RegistryEvent;
import org.tiny.mq.nameserver.store.ServiceInstance;
import org.tiny.mq.nameserver.utils.NameServerUtils;

/**
 * 处理注册事件
 */
public class RegistryListener implements Listener<RegistryEvent> {
    private static final Logger logger = LoggerFactory.getLogger(RegistryListener.class);

    @Override
    public void onReceive(RegistryEvent event) throws IllegalAccessException {
        String user = event.getUser();
        String password = event.getPassword();
        ChannelHandlerContext channelHandlerContext = event.getChannelHandlerContext();
        boolean verify = NameServerUtils.isVerify(user, password);
        if (!verify) {
            logger.info("un auth connect from {}", event.getIPAddr());
            TcpMessage message = MessageTypeEnum.ERROR_USER_MESSAGE.getTcpMessage();
            channelHandlerContext.writeAndFlush(message);
            channelHandlerContext.close();
            throw new IllegalAccessException("error account to connected!");
        }
        // 通过验证 创建id 保存到manager
        channelHandlerContext.attr(AttributeKey.valueOf("reqId")).set(event.getIPAddr());
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setBrokerIp(event.getClientIP());
        serviceInstance.setBrokerPort(event.getClientPort());
        serviceInstance.setFirstRegistryTime(System.currentTimeMillis());
        GlobalConfig.getServiceInstanceManager().put(serviceInstance);
        channelHandlerContext.writeAndFlush(MessageTypeEnum.REGISTRY_SUCCESS_MESSAGE.getTcpMessage());
        logger.info("nameserver get registry from {}", event.getIPAddr());

        // 同步给到从节点，比较严谨的同步，binlog类型，对于数据的顺序性要求很高
        // 可能是无序状态
        // 把同步的数据塞入一条队列单中，专门有一条线程从队列中提取数据，同步给各个从节点
    }
}
