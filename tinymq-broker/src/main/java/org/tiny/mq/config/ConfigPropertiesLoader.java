package org.tiny.mq.config;

import io.netty.util.internal.StringUtil;
import org.tiny.mq.common.constants.BrokerConstants;
import org.tiny.mq.model.ConfigModel;

public class ConfigPropertiesLoader {
    public void loadProperties() {
        ConfigModel globalProperties = new ConfigModel();
        String homeENV = System.getenv(BrokerConstants.TINY_MQ_HOME_PATH);
        if (StringUtil.isNullOrEmpty(homeENV)) {
            throw new IllegalArgumentException("not find env TINY_MQ_HOME");
        }
        globalProperties.setBasePath(homeENV);
        GlobalCache.setConfig(globalProperties);
    }
}
