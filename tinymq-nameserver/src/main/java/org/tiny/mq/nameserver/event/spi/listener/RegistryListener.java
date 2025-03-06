package org.tiny.mq.nameserver.event.spi.listener;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.common.coder.TcpMsg;
import org.tiny.mq.common.dto.ServiceRegistryRespDTO;
import org.tiny.mq.common.enums.NameServerResponseCode;
import org.tiny.mq.common.event.Listener;
import org.tiny.mq.nameserver.common.CommonCache;
import org.tiny.mq.nameserver.enums.ReplicationModeEnum;
import org.tiny.mq.nameserver.enums.ReplicationMsgTypeEnum;
import org.tiny.mq.nameserver.event.model.RegistryEvent;
import org.tiny.mq.nameserver.event.model.ReplicationMsgEvent;
import org.tiny.mq.nameserver.store.ServiceInstance;
import org.tiny.mq.nameserver.utils.NameserverUtils;

import java.util.UUID;

public class RegistryListener implements Listener<RegistryEvent> {

    private final Logger logger = LoggerFactory.getLogger(RegistryListener.class);

    @Override
    public void onReceive(RegistryEvent event) throws IllegalAccessException {
        //安全认证
        boolean isVerify = NameserverUtils.isVerify(event.getUser(), event.getPassword());
        ChannelHandlerContext channelHandlerContext = event.getChannelHandlerContext();
        if (!isVerify) {
            ServiceRegistryRespDTO registryRespDTO = new ServiceRegistryRespDTO();
            registryRespDTO.setMsgId(event.getMsgId());
            TcpMsg tcpMsg = new TcpMsg(NameServerResponseCode.ERROR_USER_OR_PASSWORD.getCode(),
                    JSON.toJSONBytes(registryRespDTO));
            channelHandlerContext.writeAndFlush(tcpMsg);
            channelHandlerContext.close();
            throw new IllegalAccessException("error account to connected!");
        }
        logger.info("注册事件接收:{}", JSON.toJSONString(event));
        channelHandlerContext.attr(AttributeKey.valueOf("reqId")).set(event.getIp() + ":" + event.getPort());
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setIp(event.getIp());
        serviceInstance.setPort(event.getPort());
        serviceInstance.setRegistryType(event.getRegistryType());
        serviceInstance.setAttrs(event.getAttrs());
        serviceInstance.setFirstRegistryTime(System.currentTimeMillis());
        CommonCache.getServiceInstanceManager().put(serviceInstance);
        ReplicationModeEnum replicationModeEnum = ReplicationModeEnum.of(CommonCache.getNameserverProperties().getReplicationMode());
        if (replicationModeEnum == null) {
            //单机架构，直接返回注册成功
            String msgId = event.getMsgId();
            ServiceRegistryRespDTO serviceRegistryRespDTO = new ServiceRegistryRespDTO();
            serviceRegistryRespDTO.setMsgId(msgId);
            TcpMsg tcpMsg = new TcpMsg(NameServerResponseCode.REGISTRY_SUCCESS.getCode(),
                    JSON.toJSONBytes(serviceRegistryRespDTO));
            channelHandlerContext.writeAndFlush(tcpMsg);
            return;
        }
        //如果当前是主从复制模式，而且当前角色是主节点，那么就往队列里面防元素
        ReplicationMsgEvent replicationMsgEvent = new ReplicationMsgEvent();
        replicationMsgEvent.setServiceInstance(serviceInstance);
        replicationMsgEvent.setMsgId(UUID.randomUUID().toString());
        replicationMsgEvent.setChannelHandlerContext(event.getChannelHandlerContext());
        replicationMsgEvent.setType(ReplicationMsgTypeEnum.REGISTRY.getCode());
        CommonCache.getReplicationMsgQueueManager().put(replicationMsgEvent);
        //同步给到从节点，比较严谨的同步，binlog类型，对于数据的顺序性要求很高了
        //可能是无顺序的状态
        //把同步的数据塞入一条队列当中，专门有一条线程从队列当中提取数据，同步给各个从节点
    }

}
