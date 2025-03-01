package org.tiny.mq.common.dto;

public class PullBrokerIpReqDTO extends BaseBrokerRemoteDTO {
    private String role;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "PullBrokerIpReqDTO{" +
                "role='" + role + '\'' +
                "} " + super.toString();
    }
}
