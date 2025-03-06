package org.tiny.mq.common.enums;


public enum RegistryTypeEnum {

    PRODUCER("producer"),
    CONSUMER("consumer"),
    BROKER("broker");
    String code;

    RegistryTypeEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
