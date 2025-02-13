package org.tiny.mq.nameserver.store;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReplicationChannelManager {
    private static final Map<String, ChannelHandlerContext> channelHandlerContextMap = new ConcurrentHashMap<>();

    public Map<String, ChannelHandlerContext> getChannelHandlerContextMap() {
        return channelHandlerContextMap;
    }

    public void put(String reqId, ChannelHandlerContext channelHandlerContext) {
        channelHandlerContextMap.put(reqId, channelHandlerContext);
    }


    public void get(String reqId) {
        channelHandlerContextMap.get(reqId);
    }

    public Map<String, ChannelHandlerContext> getValidSlaveChannelMap() {
        ArrayList<String> inValidChannelReqIdList = new ArrayList<>();
        for (String reqId : channelHandlerContextMap.keySet()) {
            Channel slaveChannel = channelHandlerContextMap.get(reqId).channel();
            if (slaveChannel.isActive()) {
                inValidChannelReqIdList.add(reqId);
            }
        }
        if (!inValidChannelReqIdList.isEmpty()) {
            for (String reqId : inValidChannelReqIdList) {
                channelHandlerContextMap.remove(reqId);
            }
        }
        return channelHandlerContextMap;
    }
}
