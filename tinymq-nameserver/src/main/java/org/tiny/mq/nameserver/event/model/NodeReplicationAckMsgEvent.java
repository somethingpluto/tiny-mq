package org.tiny.mq.nameserver.event.model;


import org.tiny.mq.common.event.model.Event;

public class NodeReplicationAckMsgEvent extends Event {

    private Integer type;

    private String nodeIp;

    private Integer nodePort;

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getNodeIp() {
        return nodeIp;
    }

    public void setNodeIp(String nodeIp) {
        this.nodeIp = nodeIp;
    }

    public Integer getNodePort() {
        return nodePort;
    }

    public void setNodePort(Integer nodePort) {
        this.nodePort = nodePort;
    }
}
