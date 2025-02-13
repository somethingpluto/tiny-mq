package org.tiny.mq.nameserver.eventbus.listener;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.common.codec.TcpMessage;
import org.tiny.mq.common.enums.MessageTypeEnum;
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
            logger.info("un auth connect from {}", event.getIPAddr());
            TcpMessage message = MessageTypeEnum.ERROR_USER_MESSAGE.getTcpMessage();
            channelHandlerContext.writeAndFlush(message);
            channelHandlerContext.close();
            throw new IllegalAccessException("error account to connected");
        }
        // 更新保存的信息
        String brokerIdentifyStr = (String) reqId;
        String[] brokerInfoArr = brokerIdentifyStr.split(":");
        long currentTimestamp = System.currentTimeMillis();
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setBrokerIp(brokerInfoArr[0]);
        serviceInstance.setBrokerPort(Integer.valueOf(brokerInfoArr[1]));
        serviceInstance.setLastHeartBeatTime(currentTimestamp);
        GlobalConfig.getServiceInstanceManager().putIfExist(serviceInstance);
        logger.info("accept heart beat from {}:{}", brokerInfoArr[0], brokerInfoArr[1]);
        // 通知同步信息
//        ReplicationMsgEvent replicationMsgEvent = new ReplicationMsgEvent();
//        replicationMsgEvent.setServiceInstance(serviceInstance);
//        replicationMsgEvent.setMsgId(UUID.randomUUID().toString());
//        replicationMsgEvent.setChannelHandlerContext(event.getChannelHandlerContext());
////        replicationMsgEvent.setType(ReplicationMsgTypeEnum.HEART_BEAT);
//        throw new IllegalAccessException("error account to connected");

    }
}
