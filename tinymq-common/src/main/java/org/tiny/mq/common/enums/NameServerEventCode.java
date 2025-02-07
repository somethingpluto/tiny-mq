package org.tiny.mq.common.enums;


/**
 * 注册中心事件编码
 */
public enum NameServerEventCode {

    REGISTRY(10001, "registry event"),
    UN_REGISTRY(10002, "un_registry event"),
    HEART_BEAT(10003, "heart beat"),
    START_REPLICATION(10004, "start replication"),
    MASTER_START_REPLICATION_ACK(10005, "master start answer replication"),
    MASTER_REPLICATION_MSG(10006, "master slave sync data"),
    SLAVE_HEART_BEAT(10007, "slave node heart beat"),
    SLAVE_REPLICATION_ACK_MSG(10008, "slave node sync master data success"),
    NODE_REPLICATION_MSG(10009, "slave node replication data"),
    NODE_REPLICATION_ACK_MSG(10010, "chain replicaiton data success");

    private int code;
    private String desc;

    NameServerEventCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
