package org.tiny.mq.nameserver.replication;

import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import org.tiny.mq.common.coder.TcpMsg;
import org.tiny.mq.common.dto.NodeAckDTO;
import org.tiny.mq.common.enums.NameServerEventCode;
import org.tiny.mq.nameserver.common.CommonCache;
import org.tiny.mq.nameserver.event.model.NodeReplicationMsgEvent;
import org.tiny.mq.nameserver.event.model.ReplicationMsgEvent;


public class NodeReplicationSendMsgTask extends ReplicationTask {


    public NodeReplicationSendMsgTask(String taskName) {
        super(taskName);
    }

    @Override
    void startTask() {
        while (true) {
            try {
                //如果你是头节点，不是头节点也不是尾部节点
                ReplicationMsgEvent replicationMsgEvent = CommonCache.getReplicationMsgQueueManager().getReplicationMsgQueue().take();
                Channel nextNodeChannel = CommonCache.getConnectNodeChannel();
                NodeReplicationMsgEvent nodeReplicationMsgEvent = new NodeReplicationMsgEvent();
                nodeReplicationMsgEvent.setMsgId(replicationMsgEvent.getMsgId());
                nodeReplicationMsgEvent.setServiceInstance(replicationMsgEvent.getServiceInstance());
                nodeReplicationMsgEvent.setType(replicationMsgEvent.getType());
                NodeAckDTO nodeAckDTO = new NodeAckDTO();
                //broker的连接通道
                nodeAckDTO.setChannelHandlerContext(replicationMsgEvent.getChannelHandlerContext());
                CommonCache.getNodeAckMap().put(replicationMsgEvent.getMsgId(), nodeAckDTO);
                if (nextNodeChannel.isActive()) {
                    nextNodeChannel.writeAndFlush(new TcpMsg(NameServerEventCode.NODE_REPLICATION_MSG.getCode(), JSON.toJSONBytes(nodeReplicationMsgEvent)));
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
