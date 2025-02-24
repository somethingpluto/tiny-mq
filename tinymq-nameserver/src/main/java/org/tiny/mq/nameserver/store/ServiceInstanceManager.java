package org.tiny.mq.nameserver.store;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceInstanceManager {
    private final Map<String, ServiceInstance> serviceInstanceMap = new ConcurrentHashMap<>();

    public void putIfExist(ServiceInstance serviceInstance) {
        ServiceInstance currentInstance = this.get(serviceInstance.getIp(), serviceInstance.getPort());
        if (currentInstance != null && currentInstance.getFirstRegistryTime() != null) {
            serviceInstance.setFirstRegistryTime(currentInstance.getFirstRegistryTime());
        }
        serviceInstanceMap.put(serviceInstance.getIp() + ":" + serviceInstance.getPort(), serviceInstance);
    }

    public void put(ServiceInstance serviceInstance) {
        serviceInstanceMap.put(serviceInstance.getIp() + ":" + serviceInstance.getPort(), serviceInstance);
    }

    public ServiceInstance get(String brokerIp, Integer brokerPort) {
        return serviceInstanceMap.get(brokerIp + ":" + brokerPort);
    }

    public ServiceInstance remove(String key) {
        return serviceInstanceMap.remove(key);
    }

    public Map<String, ServiceInstance> getServiceInstanceMap() {
        return serviceInstanceMap;
    }
}
