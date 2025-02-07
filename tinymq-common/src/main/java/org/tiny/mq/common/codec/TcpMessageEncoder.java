package org.tiny.mq.common.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class TcpMessageEncoder extends MessageToByteEncoder {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        TcpMessage message = (TcpMessage) o;
        byteBuf.writeShort(message.getMagic());
        byteBuf.writeInt(message.getCode());
        byteBuf.writeInt(message.getLen());
        byteBuf.writeBytes(message.getBody());
    }
}
