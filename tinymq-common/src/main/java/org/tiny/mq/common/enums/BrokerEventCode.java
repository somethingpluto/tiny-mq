package org.tiny.mq.common.enums;


public enum BrokerEventCode {

    PUSH_MSG(1001, "推送消息"),
    CONSUME_MSG(1002, "消费消息"),
    CONSUME_SUCCESS_MSG(1003, "消费成功"),
    CREATE_TOPIC(1004, "创建topic"),
    START_SYNC_MSG(1005, "开启同步服务");

    int code;
    String desc;

    BrokerEventCode(int code, String desc) {
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
