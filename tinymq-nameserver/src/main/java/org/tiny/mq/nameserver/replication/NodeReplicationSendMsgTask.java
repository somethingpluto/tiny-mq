package org.tiny.mq.nameserver.replication;

import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import org.tiny.mq.common.codec.TcpMessage;
import org.tiny.mq.common.dto.NodeAckDTO;
import org.tiny.mq.common.enums.NameServerResponseCode;
import org.tiny.mq.nameserver.config.GlobalConfig;
import org.tiny.mq.nameserver.eventbus.event.NodeReplicationMsgEvent;
import org.tiny.mq.nameserver.eventbus.event.ReplicationMsgEvent;

/**
 * 链式复制中 非尾节点将数据发送给下一个节点
 */
public class NodeReplicationSendMsgTask extends ReplicationTask {

    public NodeReplicationSendMsgTask(String taskName) {
        super(taskName);
    }

    @Override
    void startTask() {
        while (true) {
            try {
                ReplicationMsgEvent replicationMsgEvent = GlobalConfig.getReplicationMsgQueueManager().getReplicationMsgQueue().take();
                Channel connectNodeChannel = GlobalConfig.getConnectNodeChannel();
                NodeReplicationMsgEvent nodeReplicationMsgEvent = new NodeReplicationMsgEvent();
                nodeReplicationMsgEvent.setMsgId(replicationMsgEvent.getMsgId());
                nodeReplicationMsgEvent.setServiceInstance(replicationMsgEvent.getServiceInstance());
                nodeReplicationMsgEvent.setType(replicationMsgEvent.getType());
                NodeAckDTO nodeAckDTO = new NodeAckDTO();
                nodeAckDTO.setChannelHandlerContext(replicationMsgEvent.getChannelHandlerContext());
                GlobalConfig.getNodeAckMap().put(replicationMsgEvent.getMsgId(), nodeAckDTO);
                if (connectNodeChannel.isActive()) {
                    connectNodeChannel.writeAndFlush(new TcpMessage(NameServerResponseCode.NODE_REPLICATION_MSG.getCode(), JSON.toJSONBytes(nodeReplicationMsgEvent)));
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
