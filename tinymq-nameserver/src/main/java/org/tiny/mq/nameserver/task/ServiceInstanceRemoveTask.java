package org.tiny.mq.nameserver.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.nameserver.config.GlobalConfig;
import org.tiny.mq.nameserver.store.ServiceInstance;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ServiceInstanceRemoveTask implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ServiceInstanceRemoveTask.class);

    @Override
    public void run() {
        while (true) {
            try {
                TimeUnit.SECONDS.sleep(3);
                Map<String, ServiceInstance> serviceInstanceMap = GlobalConfig.getServiceInstanceManager().getServiceInstanceMap();
                long currentTimeMillis = System.currentTimeMillis();
                Iterator<String> iterator = serviceInstanceMap.keySet().iterator();
                while (iterator.hasNext()) {
                    String brokerReqId = iterator.next();
                    ServiceInstance serviceInstance = serviceInstanceMap.get(brokerReqId);
                    // 新实例还没心跳检测
                    if (serviceInstance.getLastHeartBeatTime() == null) {
                        continue;
                    }
                    // 连续3次未心跳检测
                    if (currentTimeMillis - serviceInstance.getLastHeartBeatTime() > 3000 * 3) {
                        logger.info("{} hear beat failed three times remove from broker list", brokerReqId);
                        iterator.remove();
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
