package org.tiny.mq.nameserver.enums;

public enum ReplicationRoleEnum {

    MASTER("master", "master_slave-master"),
    SLAVE("slave", "master_slave-slave"),
    NODE("not_tail_node", "chain_not_tail_node"),
    TAIL_NODE("tail_node", "chain_tail_node");

    private final String role;
    private final String desc;

    ReplicationRoleEnum(String role, String desc) {
        this.role = role;
        this.desc = desc;
    }

    public static ReplicationRoleEnum of(String role) {
        for (ReplicationRoleEnum value : values()) {
            if (value.getRole().equals(role)) {
                return value;
            }
        }
        return null;
    }

    public String getRole() {
        return role;
    }

    public String getDesc() {
        return desc;
    }
}
