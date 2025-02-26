package org.tiny.mq.nameserver.eventbus.event;

import org.tiny.mq.common.eventbus.Event;

public class StartReplicationEvent extends Event {
    private String user;
    private String password;

    private String slaveIP;
    private Integer slavePort;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSlaveIP() {
        return slaveIP;
    }

    public void setSlaveIP(String slaveIP) {
        this.slaveIP = slaveIP;
    }

    public Integer getSlavePort() {
        return slavePort;
    }

    public void setSlavePort(Integer slavePort) {
        this.slavePort = slavePort;
    }

    public String getSlaveIPAddr() {
        return slaveIP + ":" + slavePort;
    }
}
