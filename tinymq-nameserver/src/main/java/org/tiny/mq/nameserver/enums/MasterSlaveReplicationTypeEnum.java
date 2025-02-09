package org.tiny.mq.nameserver.enums;

public enum MasterSlaveReplicationTypeEnum {
    SYNC("sync", "sync replication"),  //master -> slave * 2,客户端写入数据往master写入，数据发送给到下游的slave节点，slave节点确认数据存储好之后，返回ack，需要有所有从节点返回ack，master再告诉客户端写入成功
    HALF_SYNC("half_sync", "half sync replication"),  //master -> slave * 5,客户端写入数据往master写入，数据发送给到下游的slave节点，slave节点确认数据存储好之后，返回ack，如果有超过半数节点返回ack，则master再告诉客户端写入成功
    ASYNC("async", "async replication"); //master -> slave * 2,客户端写入数据往master写入，数据写入到master的存储后，master的一个异步线程发送同步数据给到slave，快速通知客户端写入成功


    private final String type;
    private final String desc;

    MasterSlaveReplicationTypeEnum(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public static MasterSlaveReplicationTypeEnum of(String type) {
        for (MasterSlaveReplicationTypeEnum value : MasterSlaveReplicationTypeEnum.values()) {
            if (value.getType().equals(type)) {
                return value;
            }
        }
        return null;
    }

    public String getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }
}
