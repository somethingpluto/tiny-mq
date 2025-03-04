package org.tiny.mq.nameserver.eventbus.listener;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.common.codec.TcpMessage;
import org.tiny.mq.common.dto.ServiceRegistryRespDTO;
import org.tiny.mq.common.enums.NameServerResponseCode;
import org.tiny.mq.common.enums.ReplicationMsgTypeEnum;
import org.tiny.mq.common.eventbus.Listener;
import org.tiny.mq.nameserver.config.GlobalConfig;
import org.tiny.mq.nameserver.enums.ReplicationModeEnum;
import org.tiny.mq.nameserver.eventbus.event.RegistryEvent;
import org.tiny.mq.nameserver.eventbus.event.ReplicationMsgEvent;
import org.tiny.mq.nameserver.store.ServiceInstance;
import org.tiny.mq.nameserver.utils.NameServerUtils;

import java.util.UUID;

/**
 * 处理注册事件
 */
public class RegistryListener implements Listener<RegistryEvent> {
    private static final Logger logger = LoggerFactory.getLogger(RegistryListener.class);

    @Override
    public void onReceive(RegistryEvent event) throws IllegalAccessException {
        logger.info("[EVENT][Registry] type:{}, address:{}:{}", event.getRegistryType(), event.getIp(), event.getPort());
        String user = event.getUser();
        String password = event.getPassword();
        ChannelHandlerContext channelHandlerContext = event.getChannelHandlerContext();
        boolean verify = NameServerUtils.isVerify(user, password);
        if (!verify) {
            ServiceRegistryRespDTO serviceRegistryRespDTO = new ServiceRegistryRespDTO();
            serviceRegistryRespDTO.setMsgId(event.getMsgId());
            TcpMessage message = new TcpMessage(NameServerResponseCode.ERROR_USER_OR_PASSWORD.getCode(), JSON.toJSONBytes(serviceRegistryRespDTO));
            channelHandlerContext.writeAndFlush(message);
            channelHandlerContext.close();
            throw new IllegalAccessException("error account to connected!");
        }
        channelHandlerContext.attr(AttributeKey.valueOf("reqId")).set(event.getIPAddr());
        // 记录instance
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setRegistryType(event.getRegistryType());
        serviceInstance.setIp(event.getIp());
        serviceInstance.setPort(event.getPort());
        serviceInstance.setFirstRegistryTime(System.currentTimeMillis());
        GlobalConfig.getServiceInstanceManager().put(serviceInstance);
        String mode = GlobalConfig.getNameserverConfig().getReplicationMode();
        ReplicationModeEnum replicationMode = ReplicationModeEnum.of(mode);
        // 单机模式注册成功直接返回
        if (replicationMode == null) {
            String msgId = event.getMsgId();
            ServiceRegistryRespDTO serviceRegistryRespDTO = new ServiceRegistryRespDTO();
            serviceRegistryRespDTO.setMsgId(msgId);
            byte[] body = JSON.toJSONBytes(serviceRegistryRespDTO);
            channelHandlerContext.writeAndFlush(new TcpMessage(NameServerResponseCode.REGISTRY_SUCCESS.getCode(), body));
            return;
        }
        // 如果当前是主从模式，且当前节点是主节点，就往队列里面塞元素
        ReplicationMsgEvent replicationMsgEvent = new ReplicationMsgEvent();
        replicationMsgEvent.setServiceInstance(serviceInstance);
        replicationMsgEvent.setMsgId(UUID.randomUUID().toString());
        replicationMsgEvent.setChannelHandlerContext(event.getChannelHandlerContext());
        replicationMsgEvent.setType(ReplicationMsgTypeEnum.REGISTRY.getCode());
        // 将复制任务交由replication队列
        GlobalConfig.getReplicationMsgQueueManager().put(replicationMsgEvent);
        // 同步给到从节点，比较严谨的同步，binlog类型，对于数据的顺序性要求很高
        // 可能是无序状态
        // 把同步的数据塞入一条队列单中，专门有一条线程从队列中提取数据，同步给各个从节点
    }
}
