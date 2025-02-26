package org.tiny.mq.nameserver.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.common.codec.TcpMessage;
import org.tiny.mq.common.enums.NameServerEventCode;
import org.tiny.mq.common.eventbus.Event;
import org.tiny.mq.common.eventbus.EventBus;


@ChannelHandler.Sharable
public class NodeWriteMsgReplicationServerHandler extends SimpleChannelInboundHandler {
    private static final Logger logger = LoggerFactory.getLogger(NodeWriteMsgReplicationServerHandler.class);
    private final EventBus eventBus;

    public NodeWriteMsgReplicationServerHandler(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        TcpMessage message = (TcpMessage) o;
        int code = message.getCode();
        byte[] body = message.getBody();
        Event event = null;
        if (NameServerEventCode.NODE_REPLICATION_MSG.getCode() == code) {

        }
        event.setChannelHandlerContext(channelHandlerContext);
    }
}
