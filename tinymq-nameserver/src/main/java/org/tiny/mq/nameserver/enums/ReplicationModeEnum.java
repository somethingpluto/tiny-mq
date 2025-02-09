package org.tiny.mq.nameserver.enums;

public enum ReplicationModeEnum {

    MASTER_SLAVE("master_slave", "主从复制模式"),
    TRACE("trace", "链路复制模式");

    private final String mode;
    private final String desc;

    ReplicationModeEnum(String mode, String desc) {
        this.mode = mode;
        this.desc = desc;
    }

    public static ReplicationModeEnum of(String mode) {
        for (ReplicationModeEnum value :
                values()) {
            if (value.getMode().equals(mode)) {
                return value;
            }
        }
        return null;
    }

    public String getMode() {
        return mode;
    }

    public String getDesc() {
        return desc;
    }
}
