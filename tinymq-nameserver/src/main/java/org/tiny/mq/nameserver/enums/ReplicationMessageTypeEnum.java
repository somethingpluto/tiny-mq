package org.tiny.mq.nameserver.enums;

public enum ReplicationMessageTypeEnum {
    REGISTRY("registery", "节点复制"),
    HEART_BEAT("heart_beat", "心跳");
    private final String type;
    private final String desc;

    ReplicationMessageTypeEnum(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public String getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }
}
