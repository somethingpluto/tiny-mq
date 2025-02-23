package org.tiny.mq.common.remote;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import org.tiny.mq.common.codec.TcpMessageDecoder;
import org.tiny.mq.common.codec.TcpMessageEncoder;
import org.tiny.mq.common.constants.TcpConstants;

/**
 * 对NameServer服务进行访问的客户端
 */
public class NameServerNettyClient {
    private String ip;
    private Integer port;

    private NioEventLoopGroup clientGroup = new NioEventLoopGroup();
    private Bootstrap bootstrap = new Bootstrap();
    private Channel channel;

    public NameServerNettyClient(String ip, Integer port) {
        this.ip = ip;
        this.port = port;
    }

    public void buildConnection() {
        bootstrap.group(clientGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                ByteBuf delimiter = Unpooled.copiedBuffer(TcpConstants.DEFAULT_DECODE_CHAR.getBytes());
                socketChannel.pipeline().addLast(new DelimiterBasedFrameDecoder(1024 * 8, delimiter));
                socketChannel.pipeline().addLast(new TcpMessageEncoder());
                socketChannel.pipeline().addLast(new TcpMessageDecoder());
            }
        });
        try {
            ChannelFuture channelFuture = bootstrap.connect(ip, port).sync().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (!channelFuture.isSuccess()) {
                        throw new RuntimeException("connecting nameserver has error!");
                    }
                }
            });
            channel = channelFuture.channel();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
