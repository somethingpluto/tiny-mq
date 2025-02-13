package org.tiny.mq.nameserver.eventbus.event;

import io.netty.channel.ChannelHandlerContext;

public abstract class Event {
    private String msgId;

    private String clientIP;
    private Integer clientPort;

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

    public String getClientIP() {
        return clientIP;
    }

    public void setClientIP(String clientIP) {
        this.clientIP = clientIP;
    }

    public Integer getClientPort() {
        return clientPort;
    }

    public void setClientPort(Integer clientPort) {
        this.clientPort = clientPort;
    }

    public String getIPAddr() {
        return clientIP + ":" + clientPort;
    }
}
