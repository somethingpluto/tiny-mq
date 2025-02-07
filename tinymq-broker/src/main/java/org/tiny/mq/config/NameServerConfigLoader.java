package org.tiny.mq.config;

import io.netty.util.internal.StringUtil;
import org.tiny.mq.common.constants.BrokerConstants;
import org.tiny.mq.model.nameserver.NameServerConfigModel;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class NameServerConfigLoader {
    public static NameServerConfigModel loadProperties() {
        NameServerConfigModel nameServerConfigModel = new NameServerConfigModel();
        String homePath = System.getenv(BrokerConstants.TINY_MQ_HOME_PATH);
        if (StringUtil.isNullOrEmpty(homePath)) {
            throw new IllegalArgumentException("tiny_mq_home_path is null");
        }

        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(homePath + BrokerConstants.BROKER_PROPERTIES_PATH));
            nameServerConfigModel.setNameserverIP(properties.getProperty(BrokerConstants.CONFIG_NAMESERVER_IP));
            nameServerConfigModel.setNameserverPort(Integer.valueOf(properties.getProperty(BrokerConstants.CONFIG_NAMESERVER_PORT)));
            nameServerConfigModel.setNameserverUser(properties.getProperty(BrokerConstants.CONFIG_NAMESERVER_USER));
            nameServerConfigModel.setNameServerPassword(properties.getProperty(BrokerConstants.CONFIG_NAMESERVER_PASSWORD));
            nameServerConfigModel.setBrokerPort(Integer.valueOf(properties.getProperty(BrokerConstants.CONFIG_BROKER_PORT)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        GlobalCache.setNameServerConfig(nameServerConfigModel);
        return nameServerConfigModel;
    }
}
