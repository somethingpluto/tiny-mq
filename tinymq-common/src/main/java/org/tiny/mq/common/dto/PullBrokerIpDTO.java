package org.tiny.mq.common.dto;


public class PullBrokerIpDTO extends BaseNameServerRemoteDTO {

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
