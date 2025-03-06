package org.tiny.mq.nameserver.core;


import org.tiny.mq.nameserver.common.CommonCache;
import org.tiny.mq.nameserver.store.ServiceInstance;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class InValidServiceRemoveTask implements Runnable {


    @Override
    public void run() {
        while (true) {
            try {
                TimeUnit.SECONDS.sleep(3);
                Map<String, ServiceInstance> serviceInstanceMap = CommonCache.getServiceInstanceManager().getServiceInstanceMap();
                long currentTime = System.currentTimeMillis();
                Iterator<String> iterator = serviceInstanceMap.keySet().iterator();
                while (iterator.hasNext()) {
                    String brokerReqId = iterator.next();
                    ServiceInstance serviceInstance = serviceInstanceMap.get(brokerReqId);
                    if (serviceInstance.getLastHeartBeatTime() == null) {
                        continue;
                    }
                    if (currentTime - serviceInstance.getLastHeartBeatTime() > 3000 * 3) {
                        iterator.remove();
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
