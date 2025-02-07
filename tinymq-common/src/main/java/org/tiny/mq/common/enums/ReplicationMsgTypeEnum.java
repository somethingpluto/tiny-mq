package org.tiny.mq.common.enums;

public enum ReplicationMsgTypeEnum {
    REGISTRY(1, "node replication"),
    HEART_BEAT(2, "heart beat");

    private int code;
    private String desc;
    ReplicationMsgTypeEnum(int code, String desc) {
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
