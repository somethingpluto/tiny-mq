package org.tiny.mq.common.enums;


public enum AckStatus {

    SUCCESS(1),
    FAIL(0),
    ;

    AckStatus(int code) {
        this.code = code;
    }

    int code;

    public int getCode() {
        return code;
    }
}
