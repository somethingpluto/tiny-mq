package org.tiny.mq.slave;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.common.cache.BrokerServerSyncFutureManager;
import org.tiny.mq.common.coder.TcpMsg;
import org.tiny.mq.common.dto.CreateTopicReqDTO;
import org.tiny.mq.common.dto.MessageDTO;
import org.tiny.mq.common.dto.SlaveSyncRespDTO;
import org.tiny.mq.common.dto.StartSyncRespDTO;
import org.tiny.mq.common.enums.BrokerEventCode;
import org.tiny.mq.common.enums.BrokerResponseCode;
import org.tiny.mq.common.event.EventBus;
import org.tiny.mq.common.event.model.Event;
import org.tiny.mq.common.remote.SyncFuture;
import org.tiny.mq.event.model.CreateTopicEvent;
import org.tiny.mq.event.model.PushMsgEvent;

@ChannelHandler.Sharable
public class SlaveSyncServerHandler extends SimpleChannelInboundHandler {
    private static final Logger logger = LoggerFactory.getLogger(SlaveSyncServerHandler.class);

    private EventBus eventBus;

    public SlaveSyncServerHandler(EventBus eventBus) {
        this.eventBus = eventBus;
        this.eventBus.init();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        TcpMsg tcpMsg = (TcpMsg) o;
        int code = tcpMsg.getCode();
        byte[] body = tcpMsg.getBody();
        Event event = null;
        if (BrokerEventCode.CREATE_TOPIC.getCode() == code) {
            CreateTopicReqDTO createTopicReqDTO = JSON.parseObject(body, CreateTopicReqDTO.class);
            CreateTopicEvent createTopicEvent = new CreateTopicEvent();
            createTopicEvent.setCreateTopicReqDTO(createTopicReqDTO);
            createTopicEvent.setMsgId(createTopicReqDTO.getMsgId());
            event = createTopicEvent;
            event.setChannelHandlerContext(channelHandlerContext);
            eventBus.publish(event);
        } else if (BrokerEventCode.PUSH_MSG.getCode() == code) {
            MessageDTO messageDTO = JSON.parseObject(body, MessageDTO.class);
            PushMsgEvent pushMsgEvent = new PushMsgEvent();
            pushMsgEvent.setMessageDTO(messageDTO);
            pushMsgEvent.setMsgId(messageDTO.getMsgId());
            logger.info("收到消息推送内容:{},message is {}", new String(messageDTO.getBody()), JSON.toJSONString(messageDTO));
            event = pushMsgEvent;
            event.setChannelHandlerContext(channelHandlerContext);
            eventBus.publish(event);
        } else if (BrokerResponseCode.START_SYNC_SUCCESS.getCode() == code) {
            StartSyncRespDTO startSyncRespDTO = JSON.parseObject(body, StartSyncRespDTO.class);
            SyncFuture syncFuture = BrokerServerSyncFutureManager.get(startSyncRespDTO.getMsgId());
            if (syncFuture != null) {
                syncFuture.setResponse(tcpMsg);
            }
        } else if (BrokerResponseCode.SLAVE_BROKER_ACCEPT_PUSH_MSG_RESP.getCode() == code) {
            SlaveSyncRespDTO syncRespDTO = JSON.parseObject(body, SlaveSyncRespDTO.class);
            String msgId = syncRespDTO.getMsgId();
            SyncFuture syncFuture = BrokerServerSyncFutureManager.get(msgId);
            if (syncFuture != null) {
                syncFuture.setResponse(tcpMsg);
            }
        }
    }

}
