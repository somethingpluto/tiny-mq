package org.tiny.mq.nameserver.handler;

import com.alibaba.fastjson2.JSON;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.tiny.mq.common.coder.TcpMsg;
import org.tiny.mq.common.enums.NameServerEventCode;
import org.tiny.mq.common.event.EventBus;
import org.tiny.mq.common.event.model.Event;
import org.tiny.mq.nameserver.event.model.ReplicationMsgEvent;


@ChannelHandler.Sharable
public class SlaveReplicationServerHandler extends SimpleChannelInboundHandler {

    private EventBus eventBus;

    public SlaveReplicationServerHandler(EventBus eventBus) {
        this.eventBus = eventBus;
        this.eventBus.init();
    }

    //1.网络请求的接收(netty完成)
    //2.事件发布器的实现（EventBus-》event）Spring的事件，Google Guaua
    //3.事件处理器的实现（Listener-》处理event）
    //4.数据存储（基于Map本地内存的方式存储）
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
        TcpMsg tcpMsg = (TcpMsg) msg;
        int code = tcpMsg.getCode();
        byte[] body = tcpMsg.getBody();
        //从节点发起链接，在master端通过密码验证，建立链接
        Event event = null;
        if (NameServerEventCode.MASTER_REPLICATION_MSG.getCode() == code) {
            event = JSON.parseObject(body, ReplicationMsgEvent.class);
        }
        event.setChannelHandlerContext(channelHandlerContext);
        eventBus.publish(event);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
