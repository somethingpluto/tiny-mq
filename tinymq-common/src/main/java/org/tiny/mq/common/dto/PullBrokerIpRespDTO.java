package org.tiny.mq.common.dto;

import java.util.List;


public class PullBrokerIpRespDTO extends BaseNameServerRemoteDTO {

    private List<String> addressList;
    private List<String> masterList;
    private List<String> slaveList;

    public List<String> getMasterList() {
        return masterList;
    }

    public void setMasterList(List<String> masterList) {
        this.masterList = masterList;
    }

    public List<String> getSlaveList() {
        return slaveList;
    }

    public void setSlaveList(List<String> slaveList) {
        this.slaveList = slaveList;
    }

    public List<String> getAddressList() {
        return addressList;
    }

    public void setAddressList(List<String> addressList) {
        this.addressList = addressList;
    }
}
