package org.tiny.mq.common.enums;

public enum BrokerEventCode {
    PUSH_MSG(100001, "push message"),
    CONSUME_MSG(100002, "consume message"),
    CONSUME_SUCCESS_MSG(100003, "consume message success");

    int code;
    String message;

    BrokerEventCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
