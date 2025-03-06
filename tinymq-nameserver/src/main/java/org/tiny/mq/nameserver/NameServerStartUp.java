package org.tiny.mq.nameserver;

import io.netty.util.internal.StringUtil;
import org.tiny.mq.nameserver.common.CommonCache;
import org.tiny.mq.nameserver.common.TraceReplicationProperties;
import org.tiny.mq.nameserver.core.InValidServiceRemoveTask;
import org.tiny.mq.nameserver.core.NameServerStarter;
import org.tiny.mq.nameserver.enums.ReplicationModeEnum;
import org.tiny.mq.nameserver.enums.ReplicationRoleEnum;
import org.tiny.mq.nameserver.replication.*;

import java.io.IOException;

public class NameServerStartUp {

    private static NameServerStarter nameServerStarter;
    private static ReplicationService replicationService = new ReplicationService();

    private static void initReplication() {
        //复制逻辑的初始化
        ReplicationModeEnum replicationModeEnum = replicationService.checkProperties();
        //这里面会根据同步模式开启不同的netty进程
        replicationService.startReplicationTask(replicationModeEnum);
        ReplicationTask replicationTask = null;
        //开启定时任务
        if (replicationModeEnum == ReplicationModeEnum.MASTER_SLAVE) {
            ReplicationRoleEnum roleEnum = ReplicationRoleEnum.of(CommonCache.getNameserverProperties().getMasterSlaveReplicationProperties().getRole());
            if (roleEnum == ReplicationRoleEnum.MASTER) {
                replicationTask = new MasterReplicationMsgSendTask("master-replication-msg-send-task");
                replicationTask.startTaskAsync();
            } else if (roleEnum == ReplicationRoleEnum.SLAVE) {
                //发送链接主节点的请求
                //开启心跳任务，发送给主节点
                replicationTask = new SlaveReplicationHeartBeatTask("slave-replication-heart-beat-send-task");
                replicationTask.startTaskAsync();
            }
        } else if (replicationModeEnum == ReplicationModeEnum.TRACE) {
            //判断当前不是一个尾节点，开启一个复制数据的异步任务
            TraceReplicationProperties traceReplicationProperties = CommonCache.getNameserverProperties().getTraceReplicationProperties();
            if (!StringUtil.isNullOrEmpty(traceReplicationProperties.getNextNode())) {
                replicationTask = new NodeReplicationSendMsgTask("node-replication-msg-send-task");
                replicationTask.startTaskAsync();
            }
        }
        CommonCache.setReplicationTask(replicationTask);
    }

    private static void initInvalidServerRemoveTask() {
        Thread inValidServiceRemoveTask = new Thread(new InValidServiceRemoveTask());
        inValidServiceRemoveTask.setName("invalid-server-remove-task");
        inValidServiceRemoveTask.start();
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        CommonCache.getPropertiesLoader().loadProperties();
        initReplication();
        initInvalidServerRemoveTask();
        nameServerStarter = new NameServerStarter(CommonCache.getNameserverProperties().getNameserverPort());
        //阻塞
        nameServerStarter.startServer();
    }
}
