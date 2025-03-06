package org.tiny.mq.nameserver.event.model;


import org.tiny.mq.common.event.model.Event;
import org.tiny.mq.nameserver.store.ServiceInstance;

public class ReplicationMsgEvent extends Event {

    private Integer type;

    private ServiceInstance serviceInstance;

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public ServiceInstance getServiceInstance() {
        return serviceInstance;
    }

    public void setServiceInstance(ServiceInstance serviceInstance) {
        this.serviceInstance = serviceInstance;
    }
}
