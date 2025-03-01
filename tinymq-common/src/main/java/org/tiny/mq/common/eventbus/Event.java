package org.tiny.mq.common.eventbus;

import io.netty.channel.ChannelHandlerContext;

public class Event {
    private String msgId;
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

    @Override
    public String toString() {
        return "Event{" +
                "msgId='" + msgId + '\'' +
                ", channelHandlerContext=" + channelHandlerContext +
                '}';
    }
}
