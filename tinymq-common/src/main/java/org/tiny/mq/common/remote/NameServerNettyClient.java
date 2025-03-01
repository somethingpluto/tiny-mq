package org.tiny.mq.common.remote;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.tiny.mq.common.cache.NameServerSyncFutureManager;
import org.tiny.mq.common.codec.TcpMessage;
import org.tiny.mq.common.codec.TcpMessageDecoder;
import org.tiny.mq.common.codec.TcpMessageEncoder;

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
//                ByteBuf delimiter = Unpooled.copiedBuffer(TcpConstants.DEFAULT_DECODE_CHAR.getBytes());
//                socketChannel.pipeline().addLast(new DelimiterBasedFrameDecoder(1024 * 8, delimiter));
                socketChannel.pipeline().addLast(new TcpMessageEncoder());
                socketChannel.pipeline().addLast(new TcpMessageDecoder());
                socketChannel.pipeline().addLast(new NameServerRemoteRespHandler());
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

    public TcpMessage sendSyncMessage(TcpMessage tcpMessage, String msgId) {
        channel.writeAndFlush(tcpMessage);
        SyncFuture syncFuture = new SyncFuture(msgId);
        NameServerSyncFutureManager.put(msgId, syncFuture);
        try {
            // 阻塞 直到返回结果
            return (TcpMessage) syncFuture.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendAsyncMessage(TcpMessage tcpMessage) {
        channel.writeAndFlush(tcpMessage);
    }
}
