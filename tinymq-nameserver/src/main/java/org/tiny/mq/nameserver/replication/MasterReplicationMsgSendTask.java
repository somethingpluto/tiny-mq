package org.tiny.mq.nameserver.replication;

import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.tiny.mq.common.codec.TcpMessage;
import org.tiny.mq.common.dto.SlaveAckDTO;
import org.tiny.mq.common.enums.MessageTypeEnum;
import org.tiny.mq.common.enums.NameServerResponseCode;
import org.tiny.mq.nameserver.config.GlobalConfig;
import org.tiny.mq.nameserver.enums.MasterSlaveReplicationTypeEnum;
import org.tiny.mq.nameserver.eventbus.event.ReplicationMsgEvent;
import org.tiny.mq.nameserver.model.MasterSlaveReplicationConfigModel;

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
                Map<String, ChannelHandlerContext> validSlaveChannelMap = GlobalConfig.getReplicationChannelManager().getValidSlaveChannelMap();
                int validSlaveChannelCount = validSlaveChannelMap.keySet().size();
                if (replicationTypeEnum == MasterSlaveReplicationTypeEnum.ASYNC) { // 异步复制
                    this.sendMsgToSlave(replicationEvent);
                    channel.writeAndFlush(MessageTypeEnum.REGISTRY_SUCCESS_MESSAGE);
                } else if (replicationTypeEnum == MasterSlaveReplicationTypeEnum.SYNC) { // 同步复制
                    // 需要接收多少个ACK
                    this.inputMsgToAckMap(replicationEvent, validSlaveChannelCount);
                    this.sendMsgToSlave(replicationEvent);
                } else if (replicationTypeEnum == MasterSlaveReplicationTypeEnum.HALF_SYNC) { // 半同步复制，有一般的下游节点得到数据就可以
                    this.inputMsgToAckMap(replicationEvent, validSlaveChannelCount / 2);
                    this.sendMsgToSlave(replicationEvent);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
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
        // 获取所有的合法下游slave节点channel
        Map<String, ChannelHandlerContext> channelHandlerContextMap = GlobalConfig.getReplicationChannelManager().getValidSlaveChannelMap();
        for (String reqId : channelHandlerContextMap.keySet()) {
            replicationMsgEvent.setChannelHandlerContext(null);
            byte[] body = JSON.toJSONBytes(replicationMsgEvent);
            channelHandlerContextMap.get(reqId).writeAndFlush(new TcpMessage(NameServerResponseCode.MASTER_REPLICATION_MSG.getCode(), body));
        }

    }
}
