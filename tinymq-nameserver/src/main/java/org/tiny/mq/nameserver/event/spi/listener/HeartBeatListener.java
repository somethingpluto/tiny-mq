package org.tiny.mq.nameserver.event.spi.listener;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.common.coder.TcpMsg;
import org.tiny.mq.common.dto.HeartBeatDTO;
import org.tiny.mq.common.dto.ServiceRegistryRespDTO;
import org.tiny.mq.common.enums.NameServerResponseCode;
import org.tiny.mq.common.event.Listener;
import org.tiny.mq.nameserver.common.CommonCache;
import org.tiny.mq.nameserver.enums.ReplicationMsgTypeEnum;
import org.tiny.mq.nameserver.event.model.HeartBeatEvent;
import org.tiny.mq.nameserver.event.model.ReplicationMsgEvent;
import org.tiny.mq.nameserver.store.ServiceInstance;

import java.util.UUID;


public class HeartBeatListener implements Listener<HeartBeatEvent> {

    private static Logger logger = LoggerFactory.getLogger(HeartBeatListener.class);

    @Override
    public void onReceive(HeartBeatEvent event) throws IllegalAccessException {
        //把存在的实例保存下来
        ChannelHandlerContext channelHandlerContext = event.getChannelHandlerContext();
        //之前做过认证
        Object reqId = channelHandlerContext.attr(AttributeKey.valueOf("reqId")).get();
        if (reqId == null) {
            ServiceRegistryRespDTO serviceRegistryRespDTO = new ServiceRegistryRespDTO();
            serviceRegistryRespDTO.setMsgId(event.getMsgId());
            TcpMsg tcpMsg = new TcpMsg(NameServerResponseCode.ERROR_USER_OR_PASSWORD.getCode(),
                    JSON.toJSONBytes(serviceRegistryRespDTO));
            channelHandlerContext.writeAndFlush(tcpMsg);
            channelHandlerContext.close();
            throw new IllegalAccessException("error account to connected!");
        }
        logger.info("接收到心跳数据：{}", JSON.toJSONString(event));
        //心跳，客户端每隔3秒请求一次
        String reqIdStr = (String) reqId;
        String[] reqInfoStrArr = reqIdStr.split(":");
        long currentTimestamp = System.currentTimeMillis();
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setIp(reqInfoStrArr[0]);
        serviceInstance.setPort(Integer.valueOf(reqInfoStrArr[1]));
        serviceInstance.setLastHeartBeatTime(currentTimestamp);
        HeartBeatDTO heartBeatDTO = new HeartBeatDTO();
        heartBeatDTO.setMsgId(event.getMsgId());
        channelHandlerContext.writeAndFlush(new TcpMsg(NameServerResponseCode.HEART_BEAT_SUCCESS.getCode(), JSON.toJSONBytes(heartBeatDTO)));
        CommonCache.getServiceInstanceManager().putIfExist(serviceInstance);
        ReplicationMsgEvent replicationMsgEvent = new ReplicationMsgEvent();
        replicationMsgEvent.setServiceInstance(serviceInstance);
        replicationMsgEvent.setMsgId(UUID.randomUUID().toString());
        replicationMsgEvent.setChannelHandlerContext(event.getChannelHandlerContext());
        replicationMsgEvent.setType(ReplicationMsgTypeEnum.HEART_BEAT.getCode());
        CommonCache.getReplicationMsgQueueManager().put(replicationMsgEvent);
    }
}
