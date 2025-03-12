package org.tiny.mq.common.enums;

public enum BrokerClusterModeEnum {

    MASTER_SLAVE("master-slave");
    private String desc;

    BrokerClusterModeEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
