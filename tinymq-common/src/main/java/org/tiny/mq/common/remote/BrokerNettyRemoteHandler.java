package org.tiny.mq.common.remote;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.tiny.mq.common.codec.TcpMessage;

public class BrokerNettyRemoteHandler extends SimpleChannelInboundHandler {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        TcpMessage tcpMessage = (TcpMessage) o;
        int code = tcpMessage.getCode();
        byte[] body = tcpMessage.getBody();
    }
}
