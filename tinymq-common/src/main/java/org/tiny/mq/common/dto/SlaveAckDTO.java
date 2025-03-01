package org.tiny.mq.common.dto;

import io.netty.channel.ChannelHandlerContext;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

public class SlaveAckDTO implements Serializable {

    private AtomicInteger needAckTime;

    private ChannelHandlerContext brokerChannel;

    public SlaveAckDTO(AtomicInteger needAckTime, ChannelHandlerContext brokerChannel) {
        this.needAckTime = needAckTime;
        this.brokerChannel = brokerChannel;
    }

    public AtomicInteger getNeedAckTime() {
        return needAckTime;
    }

    public void setNeedAckTime(AtomicInteger needAckTime) {
        this.needAckTime = needAckTime;
    }

    public ChannelHandlerContext getBrokerChannel() {
        return brokerChannel;
    }

    public void setBrokerChannel(ChannelHandlerContext brokerChannel) {
        this.brokerChannel = brokerChannel;
    }

    @Override
    public String toString() {
        return "SlaveAckDTO{" +
                "needAckTime=" + needAckTime +
                ", brokerChannel=" + brokerChannel +
                '}';
    }
}
