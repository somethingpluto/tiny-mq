package org.tiny.mq.nameserver.eventbus.event;

import org.tiny.mq.common.eventbus.Event;

public class PullBrokerIPListEvent extends Event {
    private String role;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
