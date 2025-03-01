package org.tiny.mq.nameserver.eventbus.listener;

import com.alibaba.fastjson.JSON;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.common.codec.TcpMessage;
import org.tiny.mq.common.enums.NameServerEventCode;
import org.tiny.mq.common.eventbus.Listener;
import org.tiny.mq.nameserver.config.GlobalConfig;
import org.tiny.mq.nameserver.eventbus.event.NodeReplicationAckMsgEvent;
import org.tiny.mq.nameserver.eventbus.event.NodeReplicationMsgEvent;
import org.tiny.mq.nameserver.eventbus.event.ReplicationMsgEvent;
import org.tiny.mq.nameserver.model.TraceReplicationConfigModel;
import org.tiny.mq.nameserver.store.ServiceInstance;

import java.net.Inet4Address;

public class NodeReplicationMsgListener implements Listener<NodeReplicationMsgEvent> {
    private static final Logger logger = LoggerFactory.getLogger(NodeReplicationMsgListener.class);

    @Override
    public void onReceive(NodeReplicationMsgEvent event) throws IllegalAccessException {
        logger.info("[EVENT][Node Replication]:{}", event);
        ServiceInstance serviceInstance = event.getServiceInstance();
        // 接收上一个节点同步过来的数据
        GlobalConfig.getServiceInstanceManager().put(serviceInstance);
        ReplicationMsgEvent replicationMsgEvent = new ReplicationMsgEvent();
        replicationMsgEvent.setServiceInstance(serviceInstance);
        replicationMsgEvent.setMsgId(event.getMsgId());
        replicationMsgEvent.setType(event.getType());
        GlobalConfig.getReplicationMsgQueueManager().put(replicationMsgEvent);
        TraceReplicationConfigModel traceReplicationConfig = GlobalConfig.getNameserverConfig().getTraceReplicationConfigModel();
        // 如果是非尾节点
        if (StringUtil.isNullOrEmpty(traceReplicationConfig.getNextNode())) {
            // 需要给上一个节点返回ACK
            NodeReplicationAckMsgEvent nodeReplicationAckMsgEvent = new NodeReplicationAckMsgEvent();
            nodeReplicationAckMsgEvent.setNodeIP(Inet4Address.getLoopbackAddress().getHostAddress());
            nodeReplicationAckMsgEvent.setType(replicationMsgEvent.getType());
            nodeReplicationAckMsgEvent.setNodePort(traceReplicationConfig.getPort());
            nodeReplicationAckMsgEvent.setMsgId(replicationMsgEvent.getMsgId());
            GlobalConfig.getPreNodeChannel().writeAndFlush(new TcpMessage(NameServerEventCode.NODE_REPLICATION_MSG.getCode(), JSON.toJSONBytes(nodeReplicationAckMsgEvent)));
        }

    }
}
