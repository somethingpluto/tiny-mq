package org.tiny.mq.netty.nameserver;

import com.alibaba.fastjson.JSON;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.common.codec.TcpMessage;
import org.tiny.mq.common.codec.TcpMessageDecoder;
import org.tiny.mq.common.codec.TcpMessageEncoder;
import org.tiny.mq.common.dto.ServiceRegistryReqDTO;
import org.tiny.mq.common.enums.NameServerEventCode;
import org.tiny.mq.common.enums.RegistryTypeEnum;
import org.tiny.mq.config.GlobalCache;
import org.tiny.mq.model.nameserver.NameServerConfigModel;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 与NameServer服务端建立连接
 */
public class NameServerClient {
    private static final Logger logger = LoggerFactory.getLogger(NameServerClient.class);

    private final EventLoopGroup clientGroup = new NioEventLoopGroup();
    private final Bootstrap bootstrap = new Bootstrap();
    private final String clientIP;
    private final Integer clientPort;
    private final NameServerConfigModel nameServerConfig;
    private Channel channel;


    public NameServerClient() {
        try {
            this.clientIP = Inet4Address.getLocalHost().getHostAddress();
            this.nameServerConfig = GlobalCache.getNameServerConfig();
            this.clientPort = this.nameServerConfig.getBrokerPort();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 初始化连接
     */
    public void initConnection() {
        NameServerConfigModel nameServerConfig = GlobalCache.getNameServerConfig();
        String nameserverIP = nameServerConfig.getNameserverIP();
        Integer nameserverPort = nameServerConfig.getNameserverPort();
        bootstrap.group(clientGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                socketChannel.pipeline().addLast(new TcpMessageDecoder());
                socketChannel.pipeline().addLast(new TcpMessageEncoder());
                socketChannel.pipeline().addLast(new NameServerRespChannelHandler());
            }
        });
        ChannelFuture channelFuture = null;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            clientGroup.shutdownGracefully();
            logger.info("{}:{} client shut down", this.clientIP, this.clientPort);
        }));
        try {
            channelFuture = bootstrap.connect(nameserverIP, nameserverPort).sync();
            channel = channelFuture.channel();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Channel getChannel() {
        if (this.channel == null) {
            throw new RuntimeException("channel has not been connected");
        }
        return this.channel;
    }

    public void sendRegistryMessage() {
        ServiceRegistryReqDTO serviceRegistryReqDTO = new ServiceRegistryReqDTO();
        try {
            Map<String, Object> attrs = new HashMap<>();
            //todo 先写死
            attrs.put("role", "single");
            NameServerConfigModel nameServerConfig = GlobalCache.getNameServerConfig();
            serviceRegistryReqDTO.setIp(this.clientIP);
            serviceRegistryReqDTO.setPort(nameServerConfig.getBrokerPort());
            serviceRegistryReqDTO.setUser(nameServerConfig.getNameserverUser());
            serviceRegistryReqDTO.setPassword(nameServerConfig.getNameServerPassword());
            serviceRegistryReqDTO.setRegistryType(RegistryTypeEnum.BROKER.getType());
            serviceRegistryReqDTO.setAttrs(attrs);
            serviceRegistryReqDTO.setMsgId(UUID.randomUUID().toString());
            byte[] body = JSON.toJSONBytes(serviceRegistryReqDTO);
            TcpMessage message = new TcpMessage(NameServerEventCode.REGISTRY.getCode(), body);
            channel.writeAndFlush(message);
            logger.info("{}:{} send register request to nameserver{}:{}", this.clientIP, this.clientPort, nameServerConfig.getNameserverIP(), nameServerConfig.getNameserverPort());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getClientIP() {
        return clientIP;
    }

    public Integer getClientPort() {
        return clientPort;
    }
}
