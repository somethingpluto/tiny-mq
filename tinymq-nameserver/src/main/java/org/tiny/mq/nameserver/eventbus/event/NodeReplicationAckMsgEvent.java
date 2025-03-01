package org.tiny.mq.nameserver.eventbus.event;

import org.tiny.mq.common.eventbus.Event;

public class NodeReplicationAckMsgEvent extends Event {
    private Integer type;
    private String nodeIP;
    private Integer nodePort;

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getNodeIP() {
        return nodeIP;
    }

    public void setNodeIP(String nodeIP) {
        this.nodeIP = nodeIP;
    }

    public Integer getNodePort() {
        return nodePort;
    }

    public void setNodePort(Integer nodePort) {
        this.nodePort = nodePort;
    }

    @Override
    public String toString() {
        return "NodeReplicationAckMsgEvent{" +
                "type=" + type +
                ", nodeIP='" + nodeIP + '\'' +
                ", nodePort=" + nodePort +
                "} " + super.toString();
    }
}
