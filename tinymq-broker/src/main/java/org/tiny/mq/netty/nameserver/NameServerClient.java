package org.tiny.mq.netty.nameserver;

import com.alibaba.fastjson.JSON;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.cache.CommonCache;
import org.tiny.mq.common.coder.TcpMsg;
import org.tiny.mq.common.coder.TcpMsgDecoder;
import org.tiny.mq.common.coder.TcpMsgEncoder;
import org.tiny.mq.common.constants.TcpConstants;
import org.tiny.mq.common.dto.ServiceRegistryReqDTO;
import org.tiny.mq.common.enums.NameServerEventCode;
import org.tiny.mq.common.enums.RegistryTypeEnum;
import org.tiny.mq.config.GlobalProperties;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class NameServerClient {

    private final Logger logger = LoggerFactory.getLogger(NameServerClient.class);

    private EventLoopGroup clientGroup = new NioEventLoopGroup();
    private Bootstrap bootstrap = new Bootstrap();
    private Channel channel;
    private String DEFAULT_NAMESERVER_IP = "127.0.0.1";

    /**
     * 初始化链接
     */
    public void initConnection() {
        String ip = CommonCache.getGlobalProperties().getNameserverIp();
        Integer port = CommonCache.getGlobalProperties().getNameserverPort();
        if (StringUtil.isNullOrEmpty(ip) || port == null || port < 0) {
            throw new RuntimeException("error port or ip");
        }
        bootstrap.group(clientGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ByteBuf delimiter = Unpooled.copiedBuffer(TcpConstants.DEFAULT_DECODE_CHAR.getBytes());
                ch.pipeline().addLast(new DelimiterBasedFrameDecoder(1024 * 8, delimiter));
                ch.pipeline().addLast(new TcpMsgDecoder());
                ch.pipeline().addLast(new TcpMsgEncoder());
                ch.pipeline().addLast(new NameServerRespChannelHandler());
            }
        });
        ChannelFuture channelFuture = null;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            clientGroup.shutdownGracefully();
            logger.info("nameserver client is closed");
        }));
        try {
            channelFuture = bootstrap.connect(ip, port).sync();
            channel = channelFuture.channel();
            logger.info("success connected to nameserver!");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Channel getChannel() {
        if (channel == null) {
            throw new RuntimeException("channel has not been connected!");
        }
        return channel;
    }

    public void sendRegistryMsg() {
        ServiceRegistryReqDTO registryDTO = new ServiceRegistryReqDTO();
        try {
            Map<String, Object> attrs = new HashMap<>();
            //todo 先写死
            attrs.put("role", "single");
            //broker是主从架构,producer（主发送数据）,consumer(主，从拉数据)
            registryDTO.setIp(Inet4Address.getLocalHost().getHostAddress());
            GlobalProperties globalProperties = CommonCache.getGlobalProperties();
            registryDTO.setPort(globalProperties.getBrokerPort());
            registryDTO.setUser(globalProperties.getNameserverUser());
            registryDTO.setPassword(globalProperties.getNameserverPassword());
            registryDTO.setRegistryType(RegistryTypeEnum.BROKER.getCode());
            registryDTO.setAttrs(attrs);
            byte[] body = JSON.toJSONBytes(registryDTO);
            TcpMsg tcpMsg = new TcpMsg(NameServerEventCode.REGISTRY.getCode(), body);
            channel.writeAndFlush(tcpMsg);
            logger.info("发送注册事件");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

    }
}
