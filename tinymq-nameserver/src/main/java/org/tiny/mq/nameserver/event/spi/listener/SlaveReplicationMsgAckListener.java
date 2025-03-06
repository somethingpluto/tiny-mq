package org.tiny.mq.nameserver.event.spi.listener;


import org.tiny.mq.common.coder.TcpMsg;
import org.tiny.mq.common.dto.SlaveAckDTO;
import org.tiny.mq.common.enums.NameServerResponseCode;
import org.tiny.mq.common.event.Listener;
import org.tiny.mq.nameserver.common.CommonCache;
import org.tiny.mq.nameserver.event.model.SlaveReplicationMsgAckEvent;

public class SlaveReplicationMsgAckListener implements Listener<SlaveReplicationMsgAckEvent> {

    @Override
    public void onReceive(SlaveReplicationMsgAckEvent event) throws Exception {
        String slaveAckMsgId = event.getMsgId();
        SlaveAckDTO slaveAckDTO = CommonCache.getAckMap().get(slaveAckMsgId);
        if (slaveAckDTO == null) {
            return;
        }
        Integer currentAckTime = slaveAckDTO.getNeedAckTime().decrementAndGet();
        //如果是复制模式，代表所有从节点已经ack完毕了，
        //如果是半同步复制模式
        if (currentAckTime == 0) {
            CommonCache.getAckMap().remove(slaveAckMsgId);
            slaveAckDTO.getBrokerChannel().writeAndFlush(new TcpMsg(NameServerResponseCode.REGISTRY_SUCCESS.getCode(), NameServerResponseCode.REGISTRY_SUCCESS.getDesc().getBytes()));
        }
    }
}
