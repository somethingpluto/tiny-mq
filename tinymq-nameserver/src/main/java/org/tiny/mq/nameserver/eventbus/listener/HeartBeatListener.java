package org.tiny.mq.nameserver.eventbus.listener;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.common.codec.TcpMessage;
import org.tiny.mq.common.dto.HeartBeatDTO;
import org.tiny.mq.common.dto.ServiceRegistryRespDTO;
import org.tiny.mq.common.enums.NameServerResponseCode;
import org.tiny.mq.nameserver.config.GlobalConfig;
import org.tiny.mq.nameserver.eventbus.event.HeartBeatEvent;
import org.tiny.mq.nameserver.store.ServiceInstance;

public class HeartBeatListener implements Listener<HeartBeatEvent> {

    private static final Logger logger = LoggerFactory.getLogger(HeartBeatListener.class);

    @Override
    public void onReceive(HeartBeatEvent event) throws IllegalAccessException {
        ChannelHandlerContext channelHandlerContext = event.getChannelHandlerContext();
        Object reqId = channelHandlerContext.attr(AttributeKey.valueOf("reqId")).get();
        if (reqId == null) {
            ServiceRegistryRespDTO serviceRegistryRespDTO = new ServiceRegistryRespDTO();
            serviceRegistryRespDTO.setMsgId(event.getMsgId());
            TcpMessage message = new TcpMessage(NameServerResponseCode.ERROR_USER_OR_PASSWORD.getCode(), JSON.toJSONBytes(serviceRegistryRespDTO));
            channelHandlerContext.writeAndFlush(message);
            channelHandlerContext.close();
            throw new IllegalAccessException("error account to connected");
        }
        // 更新保存的信息
        String brokerIdentifyStr = (String) reqId;
        String[] brokerInfoArr = brokerIdentifyStr.split(":");
        long currentTimestamp = System.currentTimeMillis();
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setIp(brokerInfoArr[0]);
        serviceInstance.setPort(Integer.valueOf(brokerInfoArr[1]));
        serviceInstance.setLastHeartBeatTime(currentTimestamp);
        GlobalConfig.getServiceInstanceManager().putIfExist(serviceInstance);
        logger.info("accept heart beat from {}:{}", brokerInfoArr[0], brokerInfoArr[1]);
        // 心跳信息更新完后 给client回复
        HeartBeatDTO heartBeatDTO = new HeartBeatDTO();
        heartBeatDTO.setMsgId(event.getMsgId());
        TcpMessage tcpMessage = new TcpMessage(NameServerResponseCode.HEART_BEAT_SUCCESS.getCode(), JSON.toJSONBytes(heartBeatDTO));
        channelHandlerContext.writeAndFlush(tcpMessage);
        // 通知同步信息
//        ReplicationMsgEvent replicationMsgEvent = new ReplicationMsgEvent();
//        replicationMsgEvent.setServiceInstance(serviceInstance);
//        replicationMsgEvent.setMsgId(UUID.randomUUID().toString());
//        replicationMsgEvent.setChannelHandlerContext(event.getChannelHandlerContext());
////        replicationMsgEvent.setType(ReplicationMsgTypeEnum.HEART_BEAT);
//        throw new IllegalAccessException("error account to connected");

    }
}
