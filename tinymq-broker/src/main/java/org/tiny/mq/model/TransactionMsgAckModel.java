package org.tiny.mq.model;

import io.netty.channel.ChannelHandlerContext;

public class TransactionMsgAckModel {
    private String msgId;
    private ChannelHandlerContext channelHandlerContext;
    private long firstSendTime;

    public TransactionMsgAckModel(String msgId, ChannelHandlerContext channelHandlerContext, long firstSendTime) {
        this.msgId = msgId;
        this.channelHandlerContext = channelHandlerContext;
        this.firstSendTime = firstSendTime;
    }

    public TransactionMsgAckModel() {
    }

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

    public long getFirstSendTime() {
        return firstSendTime;
    }

    public void setFirstSendTime(long firstSendTime) {
        this.firstSendTime = firstSendTime;
    }
}
