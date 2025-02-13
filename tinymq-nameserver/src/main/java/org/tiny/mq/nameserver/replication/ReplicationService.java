package org.tiny.mq.nameserver.replication;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.common.codec.TcpMessageDecoder;
import org.tiny.mq.common.codec.TcpMessageEncoder;
import org.tiny.mq.common.utils.AssertUtils;
import org.tiny.mq.nameserver.config.GlobalConfig;
import org.tiny.mq.nameserver.enums.ReplicationModeEnum;
import org.tiny.mq.nameserver.enums.ReplicationRoleEnum;
import org.tiny.mq.nameserver.eventbus.EventBus;
import org.tiny.mq.nameserver.handler.MasterReplicationServerHandler;
import org.tiny.mq.nameserver.handler.NodeSendReplicationMsgServerHandler;
import org.tiny.mq.nameserver.handler.NodeWriteMsgReplicationServerHandler;
import org.tiny.mq.nameserver.handler.SlaveReplicationServerHandler;
import org.tiny.mq.nameserver.model.MasterSlaveReplicationConfigModel;
import org.tiny.mq.nameserver.model.NameServerConfigModel;
import org.tiny.mq.nameserver.model.TraceReplicationConfigModel;

/**
 * 开启复制任务
 */
public class ReplicationService {
    private static final Logger logger = LoggerFactory.getLogger(ReplicationService.class);

    public ReplicationModeEnum checkProperties() {
        NameServerConfigModel nameserverConfig = GlobalConfig.getNameserverConfig();
        String mode = nameserverConfig.getReplicationMode();
        if (StringUtil.isNullOrEmpty(mode)) { // 单机版本
            logger.info("stand alone mode");
            return null;
        }
        // 判断参数是否合法
        ReplicationModeEnum replicationModeEnum = ReplicationModeEnum.of(mode);
        AssertUtils.isNotNull(replicationModeEnum, "replication mode arg invalid");
        if (replicationModeEnum == ReplicationModeEnum.TRACE) { // 链路复制模式
            TraceReplicationConfigModel traceConfig = nameserverConfig.getTraceReplicationConfigModel();
            AssertUtils.isNotNull(traceConfig.getPort(), "node port is null");
        } else if (replicationModeEnum == ReplicationModeEnum.MASTER_SLAVE) { // 主从复制模式
            MasterSlaveReplicationConfigModel masterSlaveReplicationConfigModel = nameserverConfig.getMasterSlaveReplicationConfigModel();
            AssertUtils.isNotBlank(masterSlaveReplicationConfigModel.getMaster(), "master arg can not be empty");
            AssertUtils.isNotBlank(masterSlaveReplicationConfigModel.getRole(), "role arg can not be empty");
            AssertUtils.isNotBlank(masterSlaveReplicationConfigModel.getType(), "type arg can not be empty");
            AssertUtils.isNotNull(masterSlaveReplicationConfigModel.getPort(), "sync port can not be empty");
        }
        return replicationModeEnum;
    }

    // 2.根据参数判断复制的方式 开启一个netty进程，用于复制操作
    public void startReplicationTask(ReplicationModeEnum replicationModeEnum) {
        if (replicationModeEnum == null) { // 单机版本不用处理
            return;
        }
        int port = 0;
        NameServerConfigModel nameserverConfig = GlobalConfig.getNameserverConfig();
        if (replicationModeEnum == ReplicationModeEnum.MASTER_SLAVE) {
            port = nameserverConfig.getMasterSlaveReplicationConfigModel().getPort();
        } else {
            replicationModeEnum = ReplicationModeEnum.TRACE;
        }

        ReplicationRoleEnum replicationRole;
        if (replicationModeEnum == ReplicationModeEnum.MASTER_SLAVE) {
            replicationRole = ReplicationRoleEnum.of(nameserverConfig.getMasterSlaveReplicationConfigModel().getRole());
        } else {
            String nextNode = nameserverConfig.getTraceReplicationConfigModel().getNextNode();
            if (StringUtil.isNullOrEmpty(nextNode)) {
                replicationRole = ReplicationRoleEnum.TAIL_NODE;
            } else {
                replicationRole = ReplicationRoleEnum.NODE;
            }
            port = nameserverConfig.getTraceReplicationConfigModel().getPort();
        }
        int replicationPort = port;
        if (replicationRole == ReplicationRoleEnum.MASTER) { // master-slave中的master 启动服务进程
            startNettyServerAsync(new MasterReplicationServerHandler(new EventBus("master-replication-task-")), replicationPort);
        } else if (replicationRole == ReplicationRoleEnum.SLAVE) { // master-slave中的slave 启动client进程
            String masterAddress = nameserverConfig.getMasterSlaveReplicationConfigModel().getMaster();
            startNettyClientAsync(new SlaveReplicationServerHandler(new EventBus("slave-replication-task-")), masterAddress);
        } else if (replicationRole == ReplicationRoleEnum.NODE) {
            // 链式复制 节点角色
            String nextNode = nameserverConfig.getTraceReplicationConfigModel().getNextNode();
            startNettyServerAsync(new NodeSendReplicationMsgServerHandler(new EventBus("node-write -task-")), replicationPort);
            startNettyClientAsync(new NodeWriteMsgReplicationServerHandler(new EventBus("node-send-replication-msg-task-")), nextNode);
        } else if (replicationRole == ReplicationRoleEnum.TAIL_NODE) {
            startNettyServerAsync(new NodeWriteMsgReplicationServerHandler(new EventBus("node-write-msg-replication-task-")), replicationPort);
        }
    }

    /**
     * 开启netty client进程
     *
     * @param simpleChannelInboundHandler
     * @param address
     */
    private void startNettyClientAsync(SimpleChannelInboundHandler simpleChannelInboundHandler, String address) {
        Thread nettyConnectTask = new Thread(() -> {
            NioEventLoopGroup clientGroup = new NioEventLoopGroup();
            Bootstrap bootstrap = new Bootstrap();
            Channel channel;
            bootstrap.group(clientGroup);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(new TcpMessageDecoder());
                    socketChannel.pipeline().addLast(new TcpMessageEncoder());
                    socketChannel.pipeline().addLast(simpleChannelInboundHandler);
                }
            });
            ChannelFuture channelFuture = null;
            try {
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    clientGroup.shutdownGracefully();
                    logger.info("nameserver replication connect application is closed");
                }));
                String[] addr = address.split(":");
                channelFuture = bootstrap.connect(addr[0], Integer.parseInt(addr[1])).sync();
                channel = channelFuture.channel();
                logger.info("success connected to nameserver replication");
                GlobalConfig.setConnectNodeChannel(channel);
                channel.closeFuture().sync();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        nettyConnectTask.start();
    }

    /**
     * 开启一个netty server进程
     *
     * @param simpleChannelInboundHandler
     * @param port
     */
    private void startNettyServerAsync(SimpleChannelInboundHandler simpleChannelInboundHandler, int port) {
        Thread nettyServerTask = new Thread(() -> {
            //负责netty启动
            NioEventLoopGroup bossGroup = new NioEventLoopGroup();
            //处理网络io中的read&write事件
            NioEventLoopGroup workerGroup = new NioEventLoopGroup();
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup);
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.childHandler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel ch) throws Exception {
                    ch.pipeline().addLast(new TcpMessageDecoder());
                    ch.pipeline().addLast(new TcpMessageEncoder());
                    ch.pipeline().addLast(simpleChannelInboundHandler);
                }
            });
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
                logger.info("nameserver replication application is closed");
            }));
            ChannelFuture channelFuture = null;
            try {
                channelFuture = bootstrap.bind(port).sync();
                System.out.println("start nameserver's replication application on port:" + port);
                //阻塞代码
                channelFuture.channel().closeFuture().sync();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        nettyServerTask.start();
    }
}
