package org.tiny.mq.common.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.tiny.mq.common.constants.NameServerConstants;

import java.util.List;

public class TcpMessageDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        if (byteBuf.readableBytes() > 2 + 4 + 4) {
            if (byteBuf.readShort() != NameServerConstants.DEFAULT_MAGIC_NUM) {
                channelHandlerContext.close();
                return;
            }
            int code = byteBuf.readInt();
            int len = byteBuf.readInt();
            if (byteBuf.readableBytes() < len) {
                channelHandlerContext.close();
                return;
            }
            byte[] body = new byte[len];
            byteBuf.readBytes(body);
            TcpMessage tcpMessage = new TcpMessage(code, body);
            list.add(tcpMessage);
        }
    }
}
