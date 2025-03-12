package org.tiny.mq.nameserver.event.model;


import org.tiny.mq.common.event.model.Event;

public class PullBrokerIpEvent extends Event {

    private String role;

    private String brokerClusterGroup;

    public String getBrokerClusterGroup() {
        return brokerClusterGroup;
    }

    public void setBrokerClusterGroup(String brokerClusterGroup) {
        this.brokerClusterGroup = brokerClusterGroup;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
