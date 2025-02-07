package org.tiny.mq.nameserver.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class NameServerConfigModel {

    private String nameserverUser;
    private String nameserverPassword;
    private Integer nameserverPort;
    private String replicationMode;
    private TraceReplicationConfigModel traceReplicationConfigModel;
    private MasterSlaveReplicationConfigModel masterSlaveReplicationConfigModel;

    public String getNameserverUser() {
        return nameserverUser;
    }

    public void setNameserverUser(String nameserverUser) {
        this.nameserverUser = nameserverUser;
    }

    public String getNameserverPassword() {
        return nameserverPassword;
    }

    public void setNameserverPassword(String nameserverPassword) {
        this.nameserverPassword = nameserverPassword;
    }

    public Integer getNameserverPort() {
        return nameserverPort;
    }

    public void setNameserverPort(Integer nameserverPort) {
        this.nameserverPort = nameserverPort;
    }

    public String getReplicationMode() {
        return replicationMode;
    }

    public void setReplicationMode(String replicationMode) {
        this.replicationMode = replicationMode;
    }

    public TraceReplicationConfigModel getTraceReplicationConfigModel() {
        return traceReplicationConfigModel;
    }

    public void setTraceReplicationConfigModel(TraceReplicationConfigModel traceReplicationConfigModel) {
        this.traceReplicationConfigModel = traceReplicationConfigModel;
    }

    public MasterSlaveReplicationConfigModel getMasterSlaveReplicationConfigModel() {
        return masterSlaveReplicationConfigModel;
    }

    public void setMasterSlaveReplicationConfigModel(MasterSlaveReplicationConfigModel masterSlaveReplicationConfigModel) {
        this.masterSlaveReplicationConfigModel = masterSlaveReplicationConfigModel;
    }

    public void print() {
        System.out.println(JSON.toJSONString(this, SerializerFeature.PrettyFormat));
    }
}
