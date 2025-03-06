package org.tiny.mq.nameserver.event.model;


import org.tiny.mq.common.event.model.Event;

public class PullBrokerIpEvent extends Event {

    private String role;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
