package org.tiny.mq.event.spi.listener;

import com.alibaba.fastjson2.JSON;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.cache.CommonCache;
import org.tiny.mq.common.coder.TcpMsg;
import org.tiny.mq.common.dto.StartSyncRespDTO;
import org.tiny.mq.common.enums.BrokerResponseCode;
import org.tiny.mq.common.event.Listener;
import org.tiny.mq.event.model.StartSyncEvent;

import java.net.InetSocketAddress;

public class StartSyncListener implements Listener<StartSyncEvent> {
    private static final Logger logger = LoggerFactory.getLogger(StartSyncListener.class);

    @Override
    public void onReceive(StartSyncEvent event) throws Exception {
        logger.info("start sync handler,event:{}", JSON.toJSONString(event));
        InetSocketAddress inetSocketAddress = (InetSocketAddress) event.getChannelHandlerContext().channel().remoteAddress();
        String reqId = inetSocketAddress.getAddress() + ":" + inetSocketAddress.getPort();
        event.getChannelHandlerContext().attr(AttributeKey.valueOf("reqId")).set(reqId);
        CommonCache.getSlaveChannelMap().put(reqId, event.getChannelHandlerContext());
        StartSyncRespDTO startSyncRespDTO = new StartSyncRespDTO();
        startSyncRespDTO.setMsgId(event.getMsgId());
        startSyncRespDTO.setSuccess(true);
        TcpMsg tcpMsg = new TcpMsg(BrokerResponseCode.START_SYNC_SUCCESS.getCode(), JSON.toJSONBytes(startSyncRespDTO));
        event.getChannelHandlerContext().writeAndFlush(tcpMsg);
    }
}
