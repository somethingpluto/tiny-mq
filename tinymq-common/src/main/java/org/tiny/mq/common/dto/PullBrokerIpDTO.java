package org.tiny.mq.common.dto;


public class PullBrokerIpDTO extends BaseNameServerRemoteDTO {

    private String role;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
