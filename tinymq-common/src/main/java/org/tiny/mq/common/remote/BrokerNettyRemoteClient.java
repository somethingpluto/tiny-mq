package org.tiny.mq.common.remote;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.tiny.mq.common.cache.BrokerServerSyncFutureManager;
import org.tiny.mq.common.codec.TcpMessage;
import org.tiny.mq.common.codec.TcpMessageDecoder;
import org.tiny.mq.common.codec.TcpMessageEncoder;

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
                nioSocketChannel.pipeline().addLast(new TcpMessageDecoder());
                nioSocketChannel.pipeline().addLast(new TcpMessageEncoder());
                nioSocketChannel.pipeline().addLast(new BrokerNettyRemoteHandler());
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

    public TcpMessage sendSyncMessage(TcpMessage tcpMessage, String msgId) {
        channel.writeAndFlush(tcpMessage);
        SyncFuture syncFuture = new SyncFuture();
        syncFuture.setMsgId(msgId);
        BrokerServerSyncFutureManager.put(msgId, syncFuture);
        try {
            return (TcpMessage) syncFuture.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendAsyncMsg(TcpMessage tcpMsg) {
        channel.writeAndFlush(tcpMsg);
    }
}
