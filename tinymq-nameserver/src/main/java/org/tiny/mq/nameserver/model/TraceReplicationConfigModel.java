package org.tiny.mq.nameserver.model;

public class TraceReplicationConfigModel {
    private String nextNode;
    private Integer port;

    public String getNextNode() {
        return nextNode;
    }

    public void setNextNode(String nextNode) {
        this.nextNode = nextNode;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
}
