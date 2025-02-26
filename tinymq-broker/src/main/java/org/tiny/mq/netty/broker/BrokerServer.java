package org.tiny.mq.netty.broker;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.common.codec.TcpMessageDecoder;
import org.tiny.mq.common.codec.TcpMessageEncoder;
import org.tiny.mq.common.eventbus.EventBus;

public class BrokerServer {
    private static final Logger logger = LoggerFactory.getLogger(BrokerServer.class);
    private int port;
    private NioEventLoopGroup bossGroup = new NioEventLoopGroup();
    private NioEventLoopGroup workerGroup = new NioEventLoopGroup();
    private ServerBootstrap serverBootstrap;

    public BrokerServer(Integer brokerPort) {
        this.port = brokerPort;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void startServer() throws InterruptedException {
        serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup);
        serverBootstrap.channel(NioServerSocketChannel.class);
        serverBootstrap.childHandler(new ChannelInitializer<NioServerSocketChannel>() {
            @Override
            protected void initChannel(NioServerSocketChannel nioServerSocketChannel) throws Exception {
                nioServerSocketChannel.pipeline().addLast(new TcpMessageDecoder());
                nioServerSocketChannel.pipeline().addLast(new TcpMessageEncoder());
                nioServerSocketChannel.pipeline().addLast(new BrokerServerHandler(new EventBus("broker-event-bus")));
            }
        });
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            logger.info("broker server is closed");
        }));
        ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
        logger.info("start broker server on port:{}", port);
        channelFuture.channel().closeFuture().sync();
    }
}
