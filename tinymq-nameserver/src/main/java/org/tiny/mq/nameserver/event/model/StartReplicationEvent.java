package org.tiny.mq.nameserver.event.model;


import org.tiny.mq.common.event.model.Event;

public class StartReplicationEvent extends Event {

    private String user;
    private String password;
    private String slaveIp;
    private String slavePort;

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

    public String getSlaveIp() {
        return slaveIp;
    }

    public void setSlaveIp(String slaveIp) {
        this.slaveIp = slaveIp;
    }

    public String getSlavePort() {
        return slavePort;
    }

    public void setSlavePort(String slavePort) {
        this.slavePort = slavePort;
    }
}
