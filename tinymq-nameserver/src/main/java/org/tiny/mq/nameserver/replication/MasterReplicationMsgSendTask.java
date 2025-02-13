package org.tiny.mq.nameserver.replication;

import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.tiny.mq.common.codec.TcpMessage;
import org.tiny.mq.common.dto.SlaveAckDTO;
import org.tiny.mq.common.enums.NameServerResponseCode;
import org.tiny.mq.nameserver.config.GlobalConfig;
import org.tiny.mq.nameserver.enums.MasterSlaveReplicationTypeEnum;
import org.tiny.mq.nameserver.eventbus.event.ReplicationMsgEvent;
import org.tiny.mq.nameserver.model.MasterSlaveReplicationConfigModel;
import org.tiny.mq.nameserver.store.ServiceInstance;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MasterReplicationMsgSendTask extends ReplicationTask {

    public MasterReplicationMsgSendTask(String taskName) {
        super(taskName);
    }

    @Override
    void startTask() {
        MasterSlaveReplicationConfigModel masterSlaveReplicationConfig = GlobalConfig.getNameserverConfig().getMasterSlaveReplicationConfigModel();
        MasterSlaveReplicationTypeEnum replicationTypeEnum = MasterSlaveReplicationTypeEnum.of(masterSlaveReplicationConfig.getType());
        while (true) {
            try {
                ReplicationMsgEvent replicationEvent = GlobalConfig.getReplicationMsgQueueManager().getReplicationMsgQueue().take();
                Channel channel = replicationEvent.getChannelHandlerContext().channel();
                Map<String, ServiceInstance> serviceInstanceMap = GlobalConfig.getServiceInstanceManager().getServiceInstanceMap();
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     * 将需要确认的ACK信息存入map
     *
     * @param replicationMsgEvent
     * @param needAckCount
     */
    private void inputMsgToAckMap(ReplicationMsgEvent replicationMsgEvent, int needAckCount) {
        GlobalConfig.getSlaveACKMap().put(replicationMsgEvent.getMsgId(), new SlaveAckDTO(new AtomicInteger(needAckCount), replicationMsgEvent.getChannelHandlerContext()));
    }

    /**
     * 向下游从节点发送消息
     *
     * @param replicationMsgEvent
     */
    private void sendMsgToSlave(ReplicationMsgEvent replicationMsgEvent) {
        Map<String, ChannelHandlerContext> channelHandlerContextMap = GlobalConfig.getReplicationChannelManager().getValidSlaveChannelMap();
        for (String reqId : channelHandlerContextMap.keySet()) {
            replicationMsgEvent.setChannelHandlerContext(null);
            byte[] body = JSON.toJSONBytes(replicationMsgEvent);
            channelHandlerContextMap.get(reqId).writeAndFlush(new TcpMessage(NameServerResponseCode.MASTER_REPLICATION_MSG.getCode(), body));
        }

    }
}
