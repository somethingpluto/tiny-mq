package org.tiny.mq.nameserver.handler;

import com.alibaba.fastjson2.JSON;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.tiny.mq.common.coder.TcpMsg;
import org.tiny.mq.common.enums.NameServerEventCode;
import org.tiny.mq.common.event.EventBus;
import org.tiny.mq.common.event.model.Event;
import org.tiny.mq.nameserver.event.model.SlaveHeartBeatEvent;
import org.tiny.mq.nameserver.event.model.SlaveReplicationMsgAckEvent;
import org.tiny.mq.nameserver.event.model.StartReplicationEvent;


@ChannelHandler.Sharable
public class MasterReplicationServerHandler extends SimpleChannelInboundHandler {

    private EventBus eventBus;

    public MasterReplicationServerHandler(EventBus eventBus) {
        this.eventBus = eventBus;
        this.eventBus.init();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
        TcpMsg tcpMsg = (TcpMsg) msg;
        int code = tcpMsg.getCode();
        byte[] body = tcpMsg.getBody();
        Event event = null;
        if (NameServerEventCode.START_REPLICATION.getCode() == code) {
            event = handleStartReplication(body, channelHandlerContext);
        } else if (NameServerEventCode.SLAVE_HEART_BEAT.getCode() == code) {
            event = handleSlaveHeartBeat(body, channelHandlerContext);
        } else if (NameServerEventCode.SLAVE_REPLICATION_ACK_MSG.getCode() == code) {
            event = handleSlaveReplicationAckMsg(body, channelHandlerContext);
        }
        eventBus.publish(event);
    }

    private Event handleStartReplication(byte[] body, ChannelHandlerContext channelHandlerContext) {
        StartReplicationEvent startReplicationEvent = JSON.parseObject(body, StartReplicationEvent.class);
        startReplicationEvent.setChannelHandlerContext(channelHandlerContext);
        return startReplicationEvent;
    }

    private Event handleSlaveHeartBeat(byte[] body, ChannelHandlerContext channelHandlerContext) {
        SlaveHeartBeatEvent slaveHeartBeatEvent = new SlaveHeartBeatEvent();
        slaveHeartBeatEvent.setChannelHandlerContext(channelHandlerContext);
        return slaveHeartBeatEvent;
    }

    private Event handleSlaveReplicationAckMsg(byte[] body, ChannelHandlerContext channelHandlerContext) {
        SlaveReplicationMsgAckEvent slaveReplicationMsgAckEvent = JSON.parseObject(body, SlaveReplicationMsgAckEvent.class);
        slaveReplicationMsgAckEvent.setChannelHandlerContext(channelHandlerContext);
        return slaveReplicationMsgAckEvent;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
