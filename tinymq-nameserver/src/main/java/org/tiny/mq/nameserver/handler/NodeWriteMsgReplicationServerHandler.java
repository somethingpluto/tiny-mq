package org.tiny.mq.nameserver.handler;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.tiny.mq.common.coder.TcpMsg;
import org.tiny.mq.common.enums.NameServerEventCode;
import org.tiny.mq.common.event.EventBus;
import org.tiny.mq.common.event.model.Event;
import org.tiny.mq.nameserver.common.CommonCache;
import org.tiny.mq.nameserver.event.model.NodeReplicationMsgEvent;


@ChannelHandler.Sharable
public class NodeWriteMsgReplicationServerHandler extends SimpleChannelInboundHandler {

    private EventBus eventBus;

    public NodeWriteMsgReplicationServerHandler(EventBus eventBus) {
        this.eventBus = eventBus;
        this.eventBus.init();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
        TcpMsg tcpMsg = (TcpMsg) msg;
        int code = tcpMsg.getCode();
        byte[] body = tcpMsg.getBody();
        Event event = null;
        if (NameServerEventCode.NODE_REPLICATION_MSG.getCode() == code) {
            event = handleNodeReplicationMsg(body, channelHandlerContext);
        }
        CommonCache.setPreNodeChannel(channelHandlerContext.channel());
        eventBus.publish(event);
    }

    private Event handleNodeReplicationMsg(byte[] body, ChannelHandlerContext channelHandlerContext) {
        NodeReplicationMsgEvent nodeReplicationMsgEvent = JSON.parseObject(body, NodeReplicationMsgEvent.class);
        nodeReplicationMsgEvent.setChannelHandlerContext(channelHandlerContext);
        return nodeReplicationMsgEvent;
    }
}
