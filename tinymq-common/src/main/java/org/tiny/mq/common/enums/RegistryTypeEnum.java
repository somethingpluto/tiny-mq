package org.tiny.mq.common.enums;

public enum RegistryTypeEnum {
    PRODUCER("producer"),
    CONSUMER("consumer"),
    BROKER("broker");

    String type;

    RegistryTypeEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
