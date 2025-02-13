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
import org.tiny.mq.nameserver.eventbus.event.SlaveHeartBeatEvent;
import org.tiny.mq.nameserver.eventbus.event.SlaveReplicationMsgAckEvent;
import org.tiny.mq.nameserver.eventbus.event.StartReplicationEvent;

@ChannelHandler.Sharable
public class MasterReplicationServerHandler extends SimpleChannelInboundHandler {
    private static final Logger logger = LoggerFactory.getLogger(MasterReplicationServerHandler.class);
    private final EventBus eventBus;

    public MasterReplicationServerHandler(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        TcpMessage message = (TcpMessage) o;
        int code = message.getCode();
        byte[] body = message.getBody();
        Event event = null;
        // 从节点发送连接，在master节点通过密码验证，建立连接

        // 连接建立完成后master收到的数据 同步发送给slave节点

        if (NameServerEventCode.START_REPLICATION.getCode() == code) {
            event = handleStartReplicationEvent(body);
        } else if (NameServerEventCode.SLAVE_HEART_BEAT.getCode() == code) {
            event = handleSlaveHeartBeatEvent(body);
        } else if (NameServerEventCode.SLAVE_REPLICATION_ACK_MSG.getCode() == code) {
            event = handleSalveReplicationMsgAckEvent(body);
        } else {
            // TODO: 没有匹配事件
        }
        event.setChannelHandlerContext(channelHandlerContext);
        eventBus.publish(event);
    }

    public StartReplicationEvent handleStartReplicationEvent(byte[] body) {
        StartReplicationEvent event = JSON.parseObject(body, StartReplicationEvent.class);
        logger.info("salve node {} start replication", event.getSlaveIPAddr());
        return event;
    }

    public SlaveHeartBeatEvent handleSlaveHeartBeatEvent(byte[] body) {
        SlaveHeartBeatEvent event = JSON.parseObject(body, SlaveHeartBeatEvent.class);
        return event;
    }

    public SlaveReplicationMsgAckEvent handleSalveReplicationMsgAckEvent(byte[] body) {
        SlaveReplicationMsgAckEvent event = JSON.parseObject(body, SlaveReplicationMsgAckEvent.class);
        return event;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
