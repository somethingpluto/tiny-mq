package org.tiny.mq.common.enums;

public enum AckStatusEnum {
    SUCCESS(1),
    FAIL(0),
    ;

    AckStatusEnum(int code) {
        this.code = code;
    }

    int code;

    public int getCode() {
        return code;
    }
}