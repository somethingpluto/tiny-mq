package org.tiny.mq.nameserver.eventbus.event;

import org.tiny.mq.common.eventbus.Event;

public class SlaveHeartBeatEvent extends Event {
    private String ip;
    private Integer port;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getIPAddr() {
        return ip + ":" + port;
    }

    @Override
    public String toString() {
        return "SlaveHeartBeatEvent{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                "} " + super.toString();
    }
}
