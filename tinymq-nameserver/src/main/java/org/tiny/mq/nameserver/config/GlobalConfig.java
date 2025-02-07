package org.tiny.mq.nameserver.config;

import io.netty.channel.Channel;
import org.tiny.mq.nameserver.model.NameServerConfigModel;
import org.tiny.mq.nameserver.store.ReplicationChannelManager;
import org.tiny.mq.nameserver.store.ServiceInstanceManager;

public class GlobalConfig {
    private static Channel connectNodeChannel = null;
    private static Channel preNodeChannel = null;
    private static ServiceInstanceManager serviceInstanceManager = new ServiceInstanceManager();
    private static NameServerConfigModel nameserverConfig = new NameServerConfigModel();
    private static ReplicationChannelManager replicationChannelManager = new ReplicationChannelManager();
    private static ConfigLoader configLoader = new ConfigLoader();

    public static ServiceInstanceManager getServiceInstanceManager() {
        return serviceInstanceManager;
    }

    public static void setServiceInstanceManager(ServiceInstanceManager serviceInstanceManager) {
        GlobalConfig.serviceInstanceManager = serviceInstanceManager;
    }

    public static NameServerConfigModel getNameserverConfig() {
        return nameserverConfig;
    }

    public static void setNameserverConfig(NameServerConfigModel nameserverConfig) {
        GlobalConfig.nameserverConfig = nameserverConfig;
    }

    public static ReplicationChannelManager getReplicationChannelManager() {
        return replicationChannelManager;
    }

    public static void setReplicationChannelManager(ReplicationChannelManager replicationChannelManager) {
        GlobalConfig.replicationChannelManager = replicationChannelManager;
    }

    public static ConfigLoader getConfigLoader() {
        return configLoader;
    }

    public static void setConfigLoader(ConfigLoader configLoader) {
        GlobalConfig.configLoader = configLoader;
    }

    public static Channel getConnectNodeChannel() {
        return connectNodeChannel;
    }

    public static void setConnectNodeChannel(Channel connectNodeChannel) {
        GlobalConfig.connectNodeChannel = connectNodeChannel;
    }

    public static Channel getPreNodeChannel() {
        return preNodeChannel;
    }

    public static void setPreNodeChannel(Channel preNodeChannel) {
        GlobalConfig.preNodeChannel = preNodeChannel;
    }
}
