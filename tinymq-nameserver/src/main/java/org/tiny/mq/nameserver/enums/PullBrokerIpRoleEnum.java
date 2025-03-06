package org.tiny.mq.nameserver.enums;


public enum PullBrokerIpRoleEnum {

    MASTER("master"),
    SLAVE("slave"),
    SINGLE("single"),
    ;
    String code;

    PullBrokerIpRoleEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
