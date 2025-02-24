package org.tiny.mq.nameserver.config;

import org.tiny.mq.common.constants.BrokerConstants;
import org.tiny.mq.common.constants.NameServerConstants;
import org.tiny.mq.nameserver.model.MasterSlaveReplicationConfigModel;
import org.tiny.mq.nameserver.model.NameServerConfigModel;
import org.tiny.mq.nameserver.model.TraceReplicationConfigModel;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {
    private final Properties properties = new Properties();

    public void loadProperties() throws IOException {
        String homePath = System.getenv(BrokerConstants.TINY_MQ_HOME_PATH);
        properties.load(new FileInputStream(homePath + "/broker/config/nameserver.properties"));
        NameServerConfigModel nameserverConfig = new NameServerConfigModel();
        nameserverConfig.setNameserverPassword(getStr(NameServerConstants.CONFIG_PASSWORD));
        nameserverConfig.setNameserverUser(getStr(NameServerConstants.CONFIG_USER));
        nameserverConfig.setNameserverPort(getInt(NameServerConstants.CONFIG_PORT));
        nameserverConfig.setReplicationMode(getStrCanBeNull(NameServerConstants.CONFIG_REPLICATION_MODE));

        TraceReplicationConfigModel traceReplicationConfig = new TraceReplicationConfigModel();
        traceReplicationConfig.setNextNode(getStrCanBeNull(NameServerConstants.CONFIG_REPLICATION_TRACE_NEXT_NODE));
        traceReplicationConfig.setPort(getIntCanBeNull(NameServerConstants.CONFIG_REPLICATION_TRACE_PORT));

        nameserverConfig.setTraceReplicationConfigModel(traceReplicationConfig);

        MasterSlaveReplicationConfigModel masterSlaveReplicationConfig = new MasterSlaveReplicationConfigModel();
        masterSlaveReplicationConfig.setMaster(getStrCanBeNull(NameServerConstants.CONFIG_REPLICATION_MASTER));
        masterSlaveReplicationConfig.setRole(getStrCanBeNull(NameServerConstants.CONFIG_REPLICATION_MASTER_SLAVE_ROLE));
        masterSlaveReplicationConfig.setType(getStrCanBeNull(NameServerConstants.CONFIG_REPLICATION_MASTER_SLAVE_TYPE));
        masterSlaveReplicationConfig.setPort(getInt(NameServerConstants.CONFIG_REPLICATION_PORT));

        nameserverConfig.setMasterSlaveReplicationConfigModel(masterSlaveReplicationConfig);

        nameserverConfig.print();
        GlobalConfig.setNameserverConfig(nameserverConfig);
    }


    private String getStrCanBeNull(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            return "";
        }
        return value;
    }

    private String getStr(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new RuntimeException("config item：" + key + "not found");
        }
        return value;
    }

    private Integer getIntCanBeNull(String key) {
        String intValue = getStr(key);
        if (intValue == null) {
            return null;
        }
        return Integer.valueOf(intValue);
    }

    private Integer getInt(String key) {
        return Integer.valueOf(getStr(key));
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}
