package org.tiny.mq.common.dto;

import io.netty.channel.ChannelHandlerContext;

import java.io.Serializable;

public class NodeAckDTO implements Serializable {
    private ChannelHandlerContext channelHandlerContext;

    public ChannelHandlerContext getChannelHandlerContext() {
        return channelHandlerContext;
    }

    public void setChannelHandlerContext(ChannelHandlerContext channelHandlerContext) {
        this.channelHandlerContext = channelHandlerContext;
    }

    @Override
    public String toString() {
        return "NodeAckDTO{" +
                "channelHandlerContext=" + channelHandlerContext +
                '}';
    }
}
