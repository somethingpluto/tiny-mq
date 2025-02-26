package org.tiny.mq.common.enums;

public enum MessageSendWayEnum {

    SYNC(1),
    ASYNC(2),
    ;

    int code;

    MessageSendWayEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
