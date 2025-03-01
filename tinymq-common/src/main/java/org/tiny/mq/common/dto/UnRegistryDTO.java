package org.tiny.mq.common.dto;

import java.io.Serializable;

public class UnRegistryDTO implements Serializable {
    private String brokerIP;
    private Integer brokerPort;

    public String getBrokerIP() {
        return brokerIP;
    }

    public void setBrokerIP(String brokerIP) {
        this.brokerIP = brokerIP;
    }

    public Integer getBrokerPort() {
        return brokerPort;
    }

    public void setBrokerPort(Integer brokerPort) {
        this.brokerPort = brokerPort;
    }

    @Override
    public String toString() {
        return "UnRegistryDTO{" +
                "brokerIP='" + brokerIP + '\'' +
                ", brokerPort=" + brokerPort +
                '}';
    }
}
