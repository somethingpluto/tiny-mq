package org.tiny.mq.common.enums;

public enum BrokerRersponseCode {
    SEND_MSG_RESP(20001, "push message to broker"), CONSUME_MSG_RESP(20002, "consume broker message return data"), BROKER_UPDATE_CONSUME_OFFSET_RESP(20003, "broker update offset");


    int code;
    String desc;

    BrokerRersponseCode(int code, String desc) {
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
