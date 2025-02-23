package org.tiny.mq.common.remote;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import org.tiny.mq.common.codec.TcpMessageDecoder;
import org.tiny.mq.common.codec.TcpMessageEncoder;
import org.tiny.mq.common.constants.TcpConstants;

public class BrokerNettyRemoteClient {
    private String ip;
    private Integer port;

    private NioEventLoopGroup clientGroup = new NioEventLoopGroup();
    private Bootstrap bootstrap = new Bootstrap();
    private Channel channel = null;

    public BrokerNettyRemoteClient(String ip, Integer port) {
        this.ip = ip;
        this.port = port;
    }

    public void buildConnection() {
        bootstrap.group(clientGroup).channel(NioSocketChannel.class).handler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                ByteBuf delimiter = Unpooled.copiedBuffer(TcpConstants.DEFAULT_DECODE_CHAR.getBytes());
                nioSocketChannel.pipeline().addLast(new DelimiterBasedFrameDecoder(1024 * 8, delimiter));
                nioSocketChannel.pipeline().addLast(new TcpMessageDecoder());
                nioSocketChannel.pipeline().addLast(new TcpMessageEncoder());
            }
        });
        ChannelFuture channelFuture = null;
        try {
            channelFuture = bootstrap.connect(ip, port).sync().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (!channelFuture.isSuccess()) {
                        throw new RuntimeException("connection broker error");
                    }
                }
            });
            channel = channelFuture.channel();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
