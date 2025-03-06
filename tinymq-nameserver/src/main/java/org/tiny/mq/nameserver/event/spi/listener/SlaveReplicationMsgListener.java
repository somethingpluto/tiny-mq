package org.tiny.mq.nameserver.event.spi.listener;

import com.alibaba.fastjson.JSON;
import org.tiny.mq.common.coder.TcpMsg;
import org.tiny.mq.common.enums.NameServerEventCode;
import org.tiny.mq.common.event.Listener;
import org.tiny.mq.nameserver.common.CommonCache;
import org.tiny.mq.nameserver.event.model.ReplicationMsgEvent;
import org.tiny.mq.nameserver.event.model.SlaveReplicationMsgAckEvent;
import org.tiny.mq.nameserver.store.ServiceInstance;


public class SlaveReplicationMsgListener implements Listener<ReplicationMsgEvent> {


    @Override
    public void onReceive(ReplicationMsgEvent event) throws Exception {
        ServiceInstance serviceInstance = event.getServiceInstance();
        //从节点接收主节点同步数据逻辑
        CommonCache.getServiceInstanceManager().put(serviceInstance);
        SlaveReplicationMsgAckEvent slaveReplicationMsgAckEvent = new SlaveReplicationMsgAckEvent();
        slaveReplicationMsgAckEvent.setMsgId(event.getMsgId());
        event.getChannelHandlerContext().channel().writeAndFlush(new TcpMsg(NameServerEventCode.SLAVE_REPLICATION_ACK_MSG.ordinal(),
                JSON.toJSONBytes(slaveReplicationMsgAckEvent)));
    }
}
