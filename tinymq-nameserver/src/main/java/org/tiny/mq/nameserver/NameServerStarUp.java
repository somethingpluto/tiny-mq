package org.tiny.mq.nameserver;

import org.tiny.mq.nameserver.config.ConfigLoader;
import org.tiny.mq.nameserver.config.GlobalConfig;
import org.tiny.mq.nameserver.core.NameServerStarter;
import org.tiny.mq.nameserver.model.NameServerConfigModel;
import org.tiny.mq.nameserver.task.TaskManager;

import java.io.IOException;

public class NameServerStarUp {

    private static final ConfigLoader configLoader = new ConfigLoader();

    public static void main(String[] args) throws InterruptedException {

        NameServerConfigModel nameserverConfig = GlobalConfig.getNameserverConfig();
        // 获取到了集群配置的属性
        // master-slave 复制? slave链路复制
        // 主从复制：master角色开启一个额外的netty进程->slave链路接入->当数据写入master时,把写入的数据同步到slave节点
        // 链路复制: slave角色开启一个额外的进程->master连接slave
        NameServerStarter nameServerStarter = new NameServerStarter(nameserverConfig.getNameserverPort());
        nameServerStarter.startServer();
    }

    public void initConfig() throws IOException {
        configLoader.loadProperties();
    }

    public void initTask() {
        TaskManager.startInvalidServiceRemoveTask();
    }
}
