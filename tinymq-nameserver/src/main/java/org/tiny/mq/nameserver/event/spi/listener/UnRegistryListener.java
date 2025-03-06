package org.tiny.mq.nameserver.event.spi.listener;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.tiny.mq.common.coder.TcpMsg;
import org.tiny.mq.common.enums.NameServerResponseCode;
import org.tiny.mq.common.event.Listener;
import org.tiny.mq.nameserver.common.CommonCache;
import org.tiny.mq.nameserver.event.model.UnRegistryEvent;


public class UnRegistryListener implements Listener<UnRegistryEvent> {

    @Override
    public void onReceive(UnRegistryEvent event) throws IllegalAccessException {
        ChannelHandlerContext channelHandlerContext = event.getChannelHandlerContext();
        Object reqId = channelHandlerContext.attr(AttributeKey.valueOf("reqId")).get();
        if (reqId == null) {
            channelHandlerContext.writeAndFlush(new TcpMsg(NameServerResponseCode.ERROR_USER_OR_PASSWORD.getCode(),
                    NameServerResponseCode.ERROR_USER_OR_PASSWORD.getDesc().getBytes()));
            channelHandlerContext.close();
            throw new IllegalAccessException("error account to connected!");
        }
        String reqIdStr = (String) reqId;
        CommonCache.getServiceInstanceManager().remove(reqIdStr);
    }
}
