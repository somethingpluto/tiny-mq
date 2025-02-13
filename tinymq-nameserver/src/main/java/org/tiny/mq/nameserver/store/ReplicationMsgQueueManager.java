package org.tiny.mq.nameserver.store;

import org.tiny.mq.nameserver.config.GlobalConfig;
import org.tiny.mq.nameserver.enums.ReplicationModeEnum;
import org.tiny.mq.nameserver.enums.ReplicationRoleEnum;
import org.tiny.mq.nameserver.eventbus.event.ReplicationMsgEvent;
import org.tiny.mq.nameserver.model.TraceReplicationConfigModel;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * 复制信息队列管理
 */
public class ReplicationMsgQueueManager {
    private final BlockingQueue<ReplicationMsgEvent> replicationMsgQueue = new ArrayBlockingQueue<>(5000);

    public void put(ReplicationMsgEvent replicationMsgEvent) {
        String mode = GlobalConfig.getNameserverConfig().getReplicationMode();
        ReplicationModeEnum replicationMode = ReplicationModeEnum.of(mode);
        if (replicationMode == null) {
            // 单机架构 不做处理
            return;
        }
        if (replicationMode == ReplicationModeEnum.MASTER_SLAVE) {
            String role = GlobalConfig.getNameserverConfig().getMasterSlaveReplicationConfigModel().getRole();
            ReplicationRoleEnum replicationRole = ReplicationRoleEnum.of(role);
            if (replicationRole != ReplicationRoleEnum.MASTER) {
                return;
            }
            this.sendMsgToQueue(replicationMsgEvent); // 主从模式下 只有主节点能向队列发送信息
        } else if (replicationMode == ReplicationModeEnum.TRACE) {
            TraceReplicationConfigModel traceReplicationConfig = GlobalConfig.getNameserverConfig().getTraceReplicationConfigModel();
            if (traceReplicationConfig.getNextNode() != null) {
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

    public BlockingQueue<ReplicationMsgEvent> getReplicationMsgQueue() {
        return replicationMsgQueue;
    }
}
