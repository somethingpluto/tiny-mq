package org.tiny.mq.nameserver;

import io.netty.util.internal.StringUtil;
import org.tiny.mq.nameserver.config.ConfigLoader;
import org.tiny.mq.nameserver.config.GlobalConfig;
import org.tiny.mq.nameserver.core.NameServerStarter;
import org.tiny.mq.nameserver.enums.ReplicationModeEnum;
import org.tiny.mq.nameserver.enums.ReplicationRoleEnum;
import org.tiny.mq.nameserver.model.NameServerConfigModel;
import org.tiny.mq.nameserver.model.TraceReplicationConfigModel;
import org.tiny.mq.nameserver.replication.*;
import org.tiny.mq.nameserver.task.LogSysStatusTask;
import org.tiny.mq.nameserver.task.ServiceInstanceRemoveTask;

import java.io.IOException;

public class NameServerStarUp {

    private static final ConfigLoader configLoader = new ConfigLoader();
    private static final ReplicationService replicationService = new ReplicationService();
    private static NameServerStarter nameServerStarter = null;

    private static LogSysStatusTask logSysStatusTask = new LogSysStatusTask();

    private static void initReplication() {
        // 复制逻辑初始化
        ReplicationModeEnum replicationModeEnum = replicationService.checkProperties();
        replicationService.startReplicationTask(replicationModeEnum);
        ReplicationTask replicationTask = null;
        // 开启定时任务
        if (replicationModeEnum == ReplicationModeEnum.MASTER_SLAVE) {
            ReplicationRoleEnum roleEnum = ReplicationRoleEnum.of(GlobalConfig.getNameserverConfig().getMasterSlaveReplicationConfigModel().getRole());
            if (roleEnum == ReplicationRoleEnum.MASTER) {
                // 当前节点为主节点，开启Master任务
                replicationTask = new MasterReplicationMsgSendTask("master-replication-msg-send-task");
                replicationTask.startTaskAsync();
            } else if (roleEnum == ReplicationRoleEnum.SLAVE) {
                // 当前节点为从节点 开启slave任务
                replicationTask = new SlaveReplicationHeartBeatTask("slave-replication-heart-beat-send-task");
                replicationTask.startTaskAsync();
            }
        } else if (replicationModeEnum == ReplicationModeEnum.TRACE) {
            TraceReplicationConfigModel traceReplicationProperties = GlobalConfig.getNameserverConfig().getTraceReplicationConfigModel();
            if (!StringUtil.isNullOrEmpty(traceReplicationProperties.getNextNode())) {
                replicationTask = new NodeReplicationSendMsgTask("node-replication-msg-send-task");
                replicationTask.startTaskAsync();
            }
        }
        GlobalConfig.setReplicationTask(replicationTask);
    }

    private static void initInvalidServerRemoveTask() {
        Thread task = new Thread(new ServiceInstanceRemoveTask());
        task.setName("invalid-server-remove-task");
        task.start();
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        configLoader.loadProperties();
        NameServerConfigModel nameserverConfig = GlobalConfig.getNameserverConfig();
        // 获取到了集群配置的属性
        // master-slave 复制? slave链路复制
        // 主从复制：master角色开启一个额外的netty进程->slave链路接入->当数据写入master时,把写入的数据同步到slave节点
        // 链路复制: slave角色开启一个额外的进程->master连接slave
        initReplication();
        initInvalidServerRemoveTask();
        logSysStatusTask.start();
        nameServerStarter = new NameServerStarter(nameserverConfig.getNameserverPort());
        nameServerStarter.startServer();
    }
}
