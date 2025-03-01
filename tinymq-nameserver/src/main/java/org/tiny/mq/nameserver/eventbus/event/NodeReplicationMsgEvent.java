package org.tiny.mq.nameserver.eventbus.event;

import org.tiny.mq.common.eventbus.Event;
import org.tiny.mq.nameserver.store.ServiceInstance;

public class NodeReplicationMsgEvent extends Event {
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

    @Override
    public String toString() {
        return "NodeReplicationMsgEvent{" +
                "type=" + type +
                ", serviceInstance=" + serviceInstance +
                "} " + super.toString();
    }
}
