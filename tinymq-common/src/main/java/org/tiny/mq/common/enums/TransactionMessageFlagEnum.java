package org.tiny.mq.common.enums;

public enum TransactionMessageFlagEnum {

    HALF_MSG(0, "半提交消息"),
    REMAIN_HALF_ACK(1, "剩余半条ack消息");

    int code;
    String desc;

    TransactionMessageFlagEnum(int code, String desc) {
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
