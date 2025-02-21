package org.tiny.mq.nameserver.eventbus.listener;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.tiny.mq.common.codec.TcpMessage;
import org.tiny.mq.common.enums.MessageTypeEnum;
import org.tiny.mq.nameserver.config.GlobalConfig;
import org.tiny.mq.nameserver.eventbus.event.StartReplicationEvent;
import org.tiny.mq.nameserver.utils.NameServerUtils;

import java.net.InetSocketAddress;

/**
 * 主从复制开启事件处理器
 */
public class StartReplicationListener implements Listener<StartReplicationEvent> {
    @Override
    public void onReceive(StartReplicationEvent event) throws IllegalAccessException {
        boolean isVerify = NameServerUtils.isVerify(event.getUser(), event.getPassword());
        ChannelHandlerContext channelHandlerContext = event.getChannelHandlerContext();
        if (!isVerify) {
            TcpMessage message = MessageTypeEnum.ERROR_USER_MESSAGE.getTcpMessage();
            channelHandlerContext.writeAndFlush(message);
            channelHandlerContext.close();
            throw new IllegalAccessException("error account to connected!");
        }
        // 保存从节点ip port
        InetSocketAddress inetSocketAddress = (InetSocketAddress) channelHandlerContext.channel().remoteAddress();
        event.setSlaveIP(inetSocketAddress.getAddress().toString());
        event.setSlavePort(inetSocketAddress.getPort());
        String slaveIPAddr = event.getSlaveIPAddr();
        channelHandlerContext.attr(AttributeKey.valueOf("reqId")).set(slaveIPAddr);
        // 保存从节点通道
        GlobalConfig.getReplicationChannelManager().put(slaveIPAddr, channelHandlerContext);
        // 回应节点 表明已完成
        channelHandlerContext.writeAndFlush(MessageTypeEnum.MASTER_REPLICATION_MESSAGE.getTcpMessage());
    }
}
