package org.tiny.mq.common.dto;

import java.io.Serializable;

public class RegistryDTO implements Serializable {
    private String user;
    private String password;
    private String brokerIP;
    private Integer brokerPort;

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
        return "RegistryDTO{" +
                "user='" + user + '\'' +
                ", password='" + password + '\'' +
                ", brokerIP='" + brokerIP + '\'' +
                ", brokerPort=" + brokerPort +
                '}';
    }
}
