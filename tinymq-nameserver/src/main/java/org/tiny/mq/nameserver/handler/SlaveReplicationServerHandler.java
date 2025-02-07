package org.tiny.mq.nameserver.handler;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.common.codec.TcpMessage;
import org.tiny.mq.common.enums.NameServerEventCode;
import org.tiny.mq.nameserver.eventbus.EventBus;
import org.tiny.mq.nameserver.eventbus.event.Event;
import org.tiny.mq.nameserver.eventbus.event.ReplicationMsgEvent;

@ChannelHandler.Sharable
public class SlaveReplicationServerHandler extends SimpleChannelInboundHandler {

    private static final Logger logger = LoggerFactory.getLogger(SlaveReplicationServerHandler.class);
    private final EventBus eventBus;

    public SlaveReplicationServerHandler(EventBus eventBus) {
        this.eventBus = eventBus;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        TcpMessage message = (TcpMessage) o;
        int code = message.getCode();
        byte[] body = message.getBody();
        Event event = null;
        if (NameServerEventCode.MASTER_REPLICATION_MSG.getCode() == code) {
            event = handleReplicationMsgEvent(body);
        } else {
            //TODO: 没有任务匹配上
        }
        event.setChannelHandlerContext(channelHandlerContext);
        eventBus.publish(event);
    }

    public ReplicationMsgEvent handleReplicationMsgEvent(byte[] body) {
        ReplicationMsgEvent event = JSON.parseObject(body, ReplicationMsgEvent.class);
        return event;
    }
}
