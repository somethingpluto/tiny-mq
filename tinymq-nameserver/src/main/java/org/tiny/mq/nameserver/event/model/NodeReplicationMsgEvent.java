package org.tiny.mq.nameserver.event.model;


import org.tiny.mq.common.event.model.Event;
import org.tiny.mq.nameserver.store.ServiceInstance;

public class NodeReplicationMsgEvent extends Event {

    private Integer type;
    private ServiceInstance serviceInstance;

    public ServiceInstance getServiceInstance() {
        return serviceInstance;
    }

    public void setServiceInstance(ServiceInstance serviceInstance) {
        this.serviceInstance = serviceInstance;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}
