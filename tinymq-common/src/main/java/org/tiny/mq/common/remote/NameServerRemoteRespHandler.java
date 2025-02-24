package org.tiny.mq.common.remote;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.tiny.mq.common.cache.NameServerSyncFutureManager;
import org.tiny.mq.common.codec.TcpMessage;
import org.tiny.mq.common.dto.HeartBeatDTO;
import org.tiny.mq.common.dto.ServiceRegistryRespDTO;
import org.tiny.mq.common.enums.NameServerResponseCode;

@ChannelHandler.Sharable
public class NameServerRemoteRespHandler extends SimpleChannelInboundHandler {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        TcpMessage tcpMessage = (TcpMessage) o;
        int code = tcpMessage.getCode();
        byte[] body = tcpMessage.getBody();
        if (code == NameServerResponseCode.REGISTRY_SUCCESS.getCode()) {
            ServiceRegistryRespDTO serviceRegistryRespDTO = JSON.parseObject(body, ServiceRegistryRespDTO.class);
            SyncFuture syncFuture = NameServerSyncFutureManager.get(serviceRegistryRespDTO.getMsgId());
            if (syncFuture == null) {
                throw new RuntimeException("error sync future == null");
            } else {
                syncFuture.setResponse(tcpMessage);
            }
        } else if (code == NameServerResponseCode.ERROR_USER_OR_PASSWORD.getCode()) {

        } else if (code == NameServerResponseCode.HEART_BEAT_SUCCESS.getCode()) {
            HeartBeatDTO heartBeatDTO = JSON.parseObject(body, HeartBeatDTO.class);
            SyncFuture syncFuture = NameServerSyncFutureManager.get(heartBeatDTO.getMsgId());
            if (syncFuture == null) {
                throw new RuntimeException("error sync future == null");
            } else {
                syncFuture.setResponse(tcpMessage);
            }
        }
    }
}
