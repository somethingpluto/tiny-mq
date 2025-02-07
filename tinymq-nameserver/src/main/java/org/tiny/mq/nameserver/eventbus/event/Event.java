package org.tiny.mq.nameserver.eventbus.event;

import io.netty.channel.ChannelHandlerContext;

public abstract class Event {
    private String msgId;

    private String brokerIP;
    private Integer brokerPort;

    private ChannelHandlerContext channelHandlerContext;

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public ChannelHandlerContext getChannelHandlerContext() {
        return channelHandlerContext;
    }

    public void setChannelHandlerContext(ChannelHandlerContext channelHandlerContext) {
        this.channelHandlerContext = channelHandlerContext;
    }

    public String getBrokerIP() {
        return brokerIP;
    }

    public void setBrokerIP(String brokerIP) {
        this.brokerIP = brokerIP;
    }

    public Integer getBrokerPort() {
        return brokerPort;
    }

    public void setBrokerPort(Integer brokerPort) {
        this.brokerPort = brokerPort;
    }

    public String getIPAddr() {
        return brokerIP + ":" + brokerPort;
    }
}
