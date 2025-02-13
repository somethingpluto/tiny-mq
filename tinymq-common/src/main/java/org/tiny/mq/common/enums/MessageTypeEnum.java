package org.tiny.mq.common.enums;

import org.tiny.mq.common.codec.TcpMessage;

public enum MessageTypeEnum {

    ERROR_USER_MESSAGE(new TcpMessage(NameServerResponseCode.ERROR_USER_OR_PASSWORD.getCode(), NameServerResponseCode.ERROR_USER_OR_PASSWORD.getDesc().getBytes())),
    UN_REGISTRY_MESSAGE(new TcpMessage(NameServerResponseCode.UNKNOWN_EVENT.getCode(), NameServerResponseCode.UN_REGISTRY_SERVICE.getDesc().getBytes())),

    REGISTRY_SUCCESS_MESSAGE(new TcpMessage(NameServerResponseCode.REGISTRY_SUCCESS.getCode(), NameServerResponseCode.REGISTRY_SUCCESS.getDesc().getBytes())),

    MASTER_START_REPLICATION_ACK_MESSAGE(new TcpMessage(NameServerResponseCode.MASTER_START_REPLICATION_ACK.getCode(), NameServerResponseCode.MASTER_START_REPLICATION_ACK.getDesc().getBytes())),
    MASTER_REPLICATION_MESSAGE(new TcpMessage(NameServerResponseCode.MASTER_REPLICATION_MSG.getCode(), NameServerResponseCode.MASTER_REPLICATION_MSG.getDesc().getBytes()));


    private final TcpMessage tcpMessage;

    MessageTypeEnum(TcpMessage tcpMessage) {
        this.tcpMessage = tcpMessage;
    }

    public TcpMessage getTcpMessage() {
        return tcpMessage;
    }
}
