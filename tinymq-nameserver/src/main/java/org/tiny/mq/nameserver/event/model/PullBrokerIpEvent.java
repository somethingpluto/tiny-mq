package org.tiny.mq.nameserver.event.model;


import org.tiny.mq.common.event.model.Event;

public class PullBrokerIpEvent extends Event {
    // 需要拉去的broker角色
    private String role;
    // broker集群组
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
