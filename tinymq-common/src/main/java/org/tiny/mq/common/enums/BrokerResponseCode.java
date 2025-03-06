package org.tiny.mq.common.enums;


public enum BrokerResponseCode {

    SEND_MSG_RESP(2001, "推送消息给broker，响应code"),
    CONSUME_MSG_RESP(2002, "消费broker消息返回数据，响应code"),
    BROKER_UPDATE_CONSUME_OFFSET_RESP(2003, "broker更新消费offset，响应code"),
    ;

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
