package org.tiny.mq.nameserver.core;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.common.codec.TcpMessageDecoder;
import org.tiny.mq.common.codec.TcpMessageEncoder;
import org.tiny.mq.common.eventbus.EventBus;
import org.tiny.mq.nameserver.handler.TcpNettyServerHandler;

public class NameServerStarter {
    private final Logger logger = LoggerFactory.getLogger(NameServerStarter.class);
    private final int port;

    public NameServerStarter(int port) {
        this.port = port;
    }

    public void startServer() throws InterruptedException {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup);
        serverBootstrap.channel(NioServerSocketChannel.class);
        serverBootstrap.childHandler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel channel) throws Exception {
                channel.pipeline().addLast(new TcpMessageDecoder());
                channel.pipeline().addLast(new TcpMessageEncoder());
                channel.pipeline().addLast(new TcpNettyServerHandler(new EventBus("broker-connection-")));
            }
        });
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            logger.info("nameserver closed");
        }));
        ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
        logger.info("start nameserver application on port:{}", port);
        channelFuture.channel().closeFuture().sync();
    }
}
