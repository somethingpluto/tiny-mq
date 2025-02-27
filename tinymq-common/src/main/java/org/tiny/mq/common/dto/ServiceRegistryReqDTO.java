package org.tiny.mq.common.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * 服务注册请求DTO
 */
public class ServiceRegistryReqDTO extends BaseNameServerRemoteDTO {
    private String registryType;
    private String user;
    private String password;
    private String ip;
    private Integer port;
    private Map<String, Object> attrs = new HashMap<>();

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getRegistryType() {
        return registryType;
    }

    public void setRegistryType(String registryType) {
        this.registryType = registryType;
    }

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

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public Map<String, Object> getAttrs() {
        return attrs;
    }

    public void setAttrs(Map<String, Object> attrs) {
        this.attrs = attrs;
    }
}
