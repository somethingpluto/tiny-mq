package org.tiny.mq.nameserver.store;


import org.tiny.mq.nameserver.common.CommonCache;
import org.tiny.mq.nameserver.common.TraceReplicationProperties;
import org.tiny.mq.nameserver.enums.ReplicationModeEnum;
import org.tiny.mq.nameserver.enums.ReplicationRoleEnum;
import org.tiny.mq.nameserver.event.model.ReplicationMsgEvent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ReplicationMsgQueueManager {

    private BlockingQueue<ReplicationMsgEvent> replicationMsgQueue = new ArrayBlockingQueue(5000);

    public BlockingQueue<ReplicationMsgEvent> getReplicationMsgQueue() {
        return replicationMsgQueue;
    }

    public void put(ReplicationMsgEvent replicationMsgEvent) {
        ReplicationModeEnum replicationModeEnum = ReplicationModeEnum.of(CommonCache.getNameserverProperties().getReplicationMode());
        if (replicationModeEnum == null) {
            //单机架构，不做复制处理
            return;
        }
        if (replicationModeEnum == ReplicationModeEnum.MASTER_SLAVE) {
            ReplicationRoleEnum roleEnum = ReplicationRoleEnum.of(CommonCache.getNameserverProperties().getMasterSlaveReplicationProperties().getRole());
            if (roleEnum != ReplicationRoleEnum.MASTER) {
                return;
            }
            this.sendMsgToQueue(replicationMsgEvent);
        } else if (replicationModeEnum == ReplicationModeEnum.TRACE) {
            TraceReplicationProperties traceReplicationProperties = CommonCache.getNameserverProperties().getTraceReplicationProperties();
            if (traceReplicationProperties.getNextNode() != null) {
                this.sendMsgToQueue(replicationMsgEvent);
            }
        }
    }

    private void sendMsgToQueue(ReplicationMsgEvent replicationMsgEvent) {
        try {
            replicationMsgQueue.put(replicationMsgEvent);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
