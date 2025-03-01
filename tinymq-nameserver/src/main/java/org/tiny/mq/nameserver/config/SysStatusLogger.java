package org.tiny.mq.nameserver.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.nameserver.store.ServiceInstance;

import java.util.Map;

public class SysStatusLogger {
    private static final Logger logger = LoggerFactory.getLogger(SysStatusLogger.class);

    public static void logSysStatus() {
        Map<String, ServiceInstance> serviceInstanceMap = GlobalConfig.getServiceInstanceManager().getServiceInstanceMap();
        logger.info("{}", JSON.toJSONString(serviceInstanceMap, SerializerFeature.PrettyFormat));
    }
}
