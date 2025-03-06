package org.tiny.mq.common.enums;


public enum ConsumeResultStatus {

    CONSUME_SUCCESS(1),
    CONSUME_LATER(2),
    ;

    int code;

    ConsumeResultStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
