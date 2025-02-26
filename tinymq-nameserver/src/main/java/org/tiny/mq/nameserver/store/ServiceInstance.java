package org.tiny.mq.nameserver.store;

import java.util.HashMap;
import java.util.Map;

public class ServiceInstance {

    private String registryType;
    /**
     * broker IP
     */
    private String ip;
    /**
     * broker port
     */
    private Integer port;
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

    public String getRegistryType() {
        return registryType;
    }

    public void setRegistryType(String registryType) {
        this.registryType = registryType;
    }

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
        return "ServiceInstance{" + "ip='" + ip + '\'' + ", port=" + port + ", firstRegistryTime=" + firstRegistryTime + ", lastHeartBeatTime=" + lastHeartBeatTime + ", attrs=" + attrs + '}';
    }
}
