package org.tiny.mq.nameserver.enums;


public enum ReplicationMsgTypeEnum {

    REGISTRY(1, "节点复制"),
    HEART_BEAT(2, "心跳");

    ReplicationMsgTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    int code;
    String desc;

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
