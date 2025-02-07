package org.tiny.mq.nameserver.eventbus.event;

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
}
