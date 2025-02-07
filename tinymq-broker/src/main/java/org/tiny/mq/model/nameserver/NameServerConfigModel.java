package org.tiny.mq.model.nameserver;

public class NameServerConfigModel {
    private String nameserverIP;
    private Integer nameserverPort;
    private String nameserverUser;
    private String nameServerPassword;

    private Integer brokerPort;

    public String getNameserverIP() {
        return nameserverIP;
    }

    public void setNameserverIP(String nameserverIP) {
        this.nameserverIP = nameserverIP;
    }

    public Integer getNameserverPort() {
        return nameserverPort;
    }

    public void setNameserverPort(Integer nameserverPort) {
        this.nameserverPort = nameserverPort;
    }

    public String getNameserverUser() {
        return nameserverUser;
    }

    public void setNameserverUser(String nameserverUser) {
        this.nameserverUser = nameserverUser;
    }

    public String getNameServerPassword() {
        return nameServerPassword;
    }

    public void setNameServerPassword(String nameServerPassword) {
        this.nameServerPassword = nameServerPassword;
    }

    public Integer getBrokerPort() {
        return brokerPort;
    }

    public void setBrokerPort(Integer brokerPort) {
        this.brokerPort = brokerPort;
    }
}
