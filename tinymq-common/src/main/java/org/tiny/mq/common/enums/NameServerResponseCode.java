package org.tiny.mq.common.enums;

public enum NameServerResponseCode {
    ERROR_USER_OR_PASSWORD(20001, ""), UN_REGISTRY_SERVICE(20002, "service offline"), REGISTRY_SUCCESS(20003, "register success"), HEART_BEAT_SUCCESS(20004, "heart beat success"), MASTER_START_REPLICATION_ACK(20005, "master reply slave node start sync"),

    MASTER_REPLICATION_MSG(20006, "master-slave sync data"), SLAVE_HEART_BEAT(20007, "slave node heat beat"), SLAVE_REPLICATION_ACK_MSG(20008, "slave node success receive replication data"),

    NODE_REPLICATION_MSG(20009, "node replication data"), NODE_REPLICATION_ACK_MSG(20010, "chain replication data sync success"), UNKNOWN_EVENT(20000, "unknown event");

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
