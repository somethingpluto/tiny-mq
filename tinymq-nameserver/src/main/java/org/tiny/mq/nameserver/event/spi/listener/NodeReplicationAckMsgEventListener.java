package org.tiny.mq.nameserver.event.spi.listener;

import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.common.coder.TcpMsg;
import org.tiny.mq.common.dto.NodeAckDTO;
import org.tiny.mq.common.enums.NameServerEventCode;
import org.tiny.mq.common.enums.NameServerResponseCode;
import org.tiny.mq.common.event.Listener;
import org.tiny.mq.nameserver.common.CommonCache;
import org.tiny.mq.nameserver.enums.ReplicationMsgTypeEnum;
import org.tiny.mq.nameserver.event.model.NodeReplicationAckMsgEvent;


public class NodeReplicationAckMsgEventListener implements Listener<NodeReplicationAckMsgEvent> {

    private final Logger logger = LoggerFactory.getLogger(NodeReplicationAckMsgEventListener.class);

    @Override
    public void onReceive(NodeReplicationAckMsgEvent event) throws Exception {
        boolean isHeadNode = CommonCache.getPreNodeChannel() == null;
        if (isHeadNode) {
            logger.info("当前是头节点，收到下游节点ack反馈");
            //如果是头节点，直接告诉broker客户端整个链路同步复制完成
            NodeAckDTO nodeAckDTO = CommonCache.getNodeAckMap().get(event.getMsgId());
            //根据下游返回的msgId匹配先前发送端的msgId，找到broker的连接通道
            Channel brokerChannel = nodeAckDTO.getChannelHandlerContext().channel();
            if (!brokerChannel.isActive()) {
                //如果broker已经断开了，异常抛出
                throw new RuntimeException("broker connection is broken!");
            }
            CommonCache.getNodeAckMap().remove(event.getMsgId());
            if (ReplicationMsgTypeEnum.REGISTRY.getCode() == event.getType()) {
                brokerChannel.writeAndFlush(new TcpMsg(NameServerResponseCode.REGISTRY_SUCCESS.getCode(), NameServerResponseCode.REGISTRY_SUCCESS.getDesc().getBytes()));
            } else if (ReplicationMsgTypeEnum.HEART_BEAT.getCode() == event.getType()) {
                brokerChannel.writeAndFlush(new TcpMsg(NameServerResponseCode.HEART_BEAT_SUCCESS.getCode(), NameServerResponseCode.HEART_BEAT_SUCCESS.getDesc().getBytes()));
            }
        } else {
            logger.info("当前是中间节点，通知给上游节点ack回应");
            //当前节点是不是中间节点，中间节点，还得告知上一个节点同步完成
            CommonCache.getPreNodeChannel().writeAndFlush(new TcpMsg(NameServerEventCode.NODE_REPLICATION_ACK_MSG.getCode(), JSON.toJSONBytes(event)));
        }
    }
}
