package org.tiny.mq.common.enums;

public enum LocalTransactionState {
    COMMIT(0, "commit"), ROLLBACK(1, "rollback"), UNKNOWN(2, "unknown");
    int code;
    String desc;

    LocalTransactionState(int code, String desc) {
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
