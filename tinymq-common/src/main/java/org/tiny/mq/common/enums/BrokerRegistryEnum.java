package org.tiny.mq.common.enums;

public enum BrokerRegistryEnum {
    MASTER("master"), SLAVE("slave"), SINGLE("single");
    private String desc;

    BrokerRegistryEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public static BrokerRegistryEnum of(String desc) {
        for (BrokerRegistryEnum brokerEnum : BrokerRegistryEnum.values()) {
            if (desc.equals(brokerEnum.getDesc())) {
                return brokerEnum;
            }
        }
        return null;
    }
}
