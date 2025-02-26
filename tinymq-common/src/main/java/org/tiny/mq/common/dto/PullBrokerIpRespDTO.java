package org.tiny.mq.common.dto;

import java.util.List;

public class PullBrokerIpRespDTO extends BaseBrokerRemoteDTO {
    private List<String> addressList;

    public List<String> getAddressList() {
        return addressList;
    }

    public void setAddressList(List<String> addressList) {
        this.addressList = addressList;
    }
}
