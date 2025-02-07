package org.tiny.mq.common.enums;

public enum NameServerResponseCode {
    ERROR_USER_OR_PASSWORD(20001, ""),
    UN_REGISTRY_SERVICE(20002, "service offline"),
    REGISTRY_SUCCESS(20003, "register success"),
    HEART_BEAT_SUCCESS(20004, "heart beat success"),

    UNKNOWN_EVENT(20000, "unknown event");

    private int code;
    private String desc;

    NameServerResponseCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
