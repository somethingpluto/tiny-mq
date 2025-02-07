package org.tiny.mq.nameserver.store;

import java.util.HashMap;
import java.util.Map;

public class ServiceInstance {

    /**
     * broker IP
     */
    private String brokerIp;
    /**
     * broker port
     */
    private Integer brokerPort;
    /**
     * broker 注册事件
     */
    private Long firstRegistryTime;
    /**
     * broker 最后一次心跳通信时间
     */
    private Long lastHeartBeatTime;
    /**
     * broker额外信息
     */
    private Map<String, String> attrs = new HashMap<>();

    public String getBrokerIp() {
        return brokerIp;
    }

    public void setBrokerIp(String brokerIp) {
        this.brokerIp = brokerIp;
    }

    public Integer getBrokerPort() {
        return brokerPort;
    }

    public void setBrokerPort(Integer brokerPort) {
        this.brokerPort = brokerPort;
    }

    public Long getFirstRegistryTime() {
        return firstRegistryTime;
    }

    public void setFirstRegistryTime(Long firstRegistryTime) {
        this.firstRegistryTime = firstRegistryTime;
    }

    public Long getLastHeartBeatTime() {
        return lastHeartBeatTime;
    }

    public void setLastHeartBeatTime(Long lastHeartBeatTime) {
        this.lastHeartBeatTime = lastHeartBeatTime;
    }

    public Map<String, String> getAttrs() {
        return attrs;
    }

    public void setAttrs(Map<String, String> attrs) {
        this.attrs = attrs;
    }


    @Override
    public String toString() {
        return "ServiceInstance{" + "brokerIp='" + brokerIp + '\'' + ", brokerPort=" + brokerPort + ", firstRegistryTime=" + firstRegistryTime + ", lastHeartBeatTime=" + lastHeartBeatTime + ", attrs=" + attrs + '}';
    }
}
