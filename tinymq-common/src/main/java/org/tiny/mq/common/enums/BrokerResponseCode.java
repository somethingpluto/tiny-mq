package org.tiny.mq.common.enums;


public enum BrokerResponseCode {

    SEND_MSG_RESP(2001, "推送消息给broker，响应code"),
    CONSUME_MSG_RESP(2002, "消费broker消息返回数据，响应code"),
    BROKER_UPDATE_CONSUME_OFFSET_RESP(2003, "broker更新消费offset，响应code"),
    TOPIC_ALREADY_EXIST(2004, "topic已存在"),
    QUEUE_SIZE_TOO_LARGE(2005, "queue size 太大"),
    START_SYNC_SUCCESS(2006, "开启同步模式成功");

    int code;
    String desc;

    BrokerResponseCode(int code, String desc) {
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
