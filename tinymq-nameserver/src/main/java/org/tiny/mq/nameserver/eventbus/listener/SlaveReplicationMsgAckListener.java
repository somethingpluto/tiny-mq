package org.tiny.mq.nameserver.eventbus.listener;

import org.tiny.mq.common.dto.SlaveAckDTO;
import org.tiny.mq.common.enums.MessageTypeEnum;
import org.tiny.mq.common.eventbus.Listener;
import org.tiny.mq.nameserver.config.GlobalConfig;
import org.tiny.mq.nameserver.eventbus.event.SlaveReplicationMsgAckEvent;

/**
 * 从节点回复复制同步信息
 */
public class SlaveReplicationMsgAckListener implements Listener<SlaveReplicationMsgAckEvent> {
    @Override
    public void onReceive(SlaveReplicationMsgAckEvent event) throws IllegalAccessException {
        String msgId = event.getMsgId();
        SlaveAckDTO slaveAckDTO = GlobalConfig.getSlaveACKMap().get(msgId);
        if (slaveAckDTO == null) {
            return;
        }
        // 根据获取到的减一
        int currentAckTime = slaveAckDTO.getNeedAckTime().decrementAndGet();
        if (currentAckTime == 0) {
            GlobalConfig.getSlaveACKMap().remove(msgId);
            slaveAckDTO.getBrokerChannel().writeAndFlush(MessageTypeEnum.REGISTRY_SUCCESS_MESSAGE);
        }
    }
}
