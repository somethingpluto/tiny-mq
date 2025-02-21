package org.tiny.mq.nameserver.eventbus.listener;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.common.codec.TcpMessage;
import org.tiny.mq.common.enums.NameServerEventCode;
import org.tiny.mq.nameserver.config.GlobalConfig;
import org.tiny.mq.nameserver.eventbus.event.ReplicationMsgEvent;
import org.tiny.mq.nameserver.eventbus.event.SlaveReplicationMsgAckEvent;
import org.tiny.mq.nameserver.replication.SlaveReplicationHeartBeatTask;
import org.tiny.mq.nameserver.store.ServiceInstance;

public class SlaveReplicationMsgListener implements Listener<ReplicationMsgEvent> {
    private static final Logger logger = LoggerFactory.getLogger(SlaveReplicationHeartBeatTask.class);

    @Override
    public void onReceive(ReplicationMsgEvent event) {
        ServiceInstance serviceInstance = event.getServiceInstance();
        logger.info("slave node {}:{} replication heart beat", serviceInstance.getBrokerIp(), serviceInstance.getBrokerPort());
        GlobalConfig.getServiceInstanceManager().put(serviceInstance);
        SlaveReplicationMsgAckEvent slaveReplicationMsgAckEvent = new SlaveReplicationMsgAckEvent();
        slaveReplicationMsgAckEvent.setMsgId(event.getMsgId());
        event.getChannelHandlerContext().channel().writeAndFlush(new TcpMessage(NameServerEventCode.SLAVE_REPLICATION_ACK_MSG.getCode(), JSON.toJSONBytes(slaveReplicationMsgAckEvent)));
    }
}
