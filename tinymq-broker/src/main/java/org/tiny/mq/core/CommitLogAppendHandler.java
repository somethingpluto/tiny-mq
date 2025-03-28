package org.tiny.mq.core;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.cache.CommonCache;
import org.tiny.mq.common.cache.BrokerServerSyncFutureManager;
import org.tiny.mq.common.coder.TcpMsg;
import org.tiny.mq.common.constants.BrokerConstants;
import org.tiny.mq.common.dto.MessageDTO;
import org.tiny.mq.common.dto.SendMessageToBrokerResponseDTO;
import org.tiny.mq.common.enums.*;
import org.tiny.mq.common.remote.SyncFuture;
import org.tiny.mq.event.model.PushMsgEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class CommitLogAppendHandler {

    private static final Logger logger = LoggerFactory.getLogger(CommitLogAppendHandler.class);

    public void prepareMMapLoading(String topicName) throws IOException {
        CommitLogMMapFileModel mapFileModel = new CommitLogMMapFileModel();
        mapFileModel.loadFileInMMap(topicName, 0, BrokerConstants.COMMIT_LOG_DEFAULT_MMAP_SIZE);
        CommonCache.getCommitLogMMapFileModelManager().put(topicName, mapFileModel);
    }

    public void appendMsg(MessageDTO messageDTO) throws IOException {
        CommitLogMMapFileModel mapFileModel = CommonCache.getCommitLogMMapFileModelManager().get(messageDTO.getTopic());
        if (mapFileModel == null) {
            throw new RuntimeException("topic is invalid!");
        }
        mapFileModel.writeContent(messageDTO, true);
    }

    public void appendMsg(PushMsgEvent event) {
        MessageDTO messageDTO = event.getMessageDTO();
        boolean isAsyncSend = messageDTO.getSendWay() == MessageSendWay.ASYNC.getCode();
        String brokerMode = CommonCache.getGlobalProperties().getBrokerClusterMode();
        String brokerRole = CommonCache.getGlobalProperties().getBrokerClusterRole();
        if (brokerMode.equals(BrokerClusterModeEnum.MASTER_SLAVE.getDesc())) {
            if (brokerRole.equals(BrokerRegistryEnum.MASTER.getDesc())) {  // 主节点同步行为
                if (isAsyncSend) { //
                    masterAsyncSlaveBroker(event);
                } else {
                    masterSyncSlaveBroker(event);
                }
            } else { // 从节点接收完成同步动作 响应主节点
                slaveBrokerSendRespToMaster(event);
            }

        } else {// 单机模式
            if (isAsyncSend) { // 异步返回
                return;
            } else { // 同步返回
                singleModeSyncToBroker(event);
            }
        }
    }


    private void slaveBrokerSendRespToMaster(PushMsgEvent event) {
        MessageDTO messageDTO = event.getMessageDTO();
        int sendWay = messageDTO.getSendWay();
        if (sendWay == MessageSendWay.ASYNC.getCode()) {
            return;
        }
        if (sendWay == MessageSendWay.SYNC.getCode()) {
            SendMessageToBrokerResponseDTO sendMessageToBrokerResponseDTO = new SendMessageToBrokerResponseDTO();
            sendMessageToBrokerResponseDTO.setMsgId(messageDTO.getMsgId());
            sendMessageToBrokerResponseDTO.setStatus(SendMessageToBrokerResponseStatus.SUCCESS.getCode());
            TcpMsg tcpMsg = new TcpMsg(BrokerResponseCode.SLAVE_BROKER_ACCEPT_PUSH_MSG_RESP.getCode(), JSON.toJSONBytes(sendMessageToBrokerResponseDTO));
            event.getChannelHandlerContext().writeAndFlush(tcpMsg);
        }
    }

    private void masterSyncSlaveBroker(PushMsgEvent event) {
        MessageDTO messageDTO = event.getMessageDTO();
        ArrayList<String> needAckMsgIdList = new ArrayList<>();
        for (ChannelHandlerContext channelHandlerContext : CommonCache.getSlaveChannelMap().values()) {
            MessageDTO toSlaveMessageDTO = new MessageDTO();
            String msgId = UUID.randomUUID().toString();
            toSlaveMessageDTO.setBody(messageDTO.getBody());
            toSlaveMessageDTO.setTopic(messageDTO.getTopic());
            toSlaveMessageDTO.setQueueId(messageDTO.getQueueId());
            toSlaveMessageDTO.setSendWay(messageDTO.getSendWay());
            toSlaveMessageDTO.setMsgId(msgId);
            TcpMsg tcpMsg = new TcpMsg(BrokerEventCode.PUSH_MSG.getCode(), JSON.toJSONBytes(toSlaveMessageDTO));
            SyncFuture syncFuture = new SyncFuture();
            syncFuture.setMsgId(msgId);
            BrokerServerSyncFutureManager.put(msgId, syncFuture);
            needAckMsgIdList.add(msgId);
            channelHandlerContext.writeAndFlush(tcpMsg);
        }
        // 开启同步等待模式
        for (String msgId : needAckMsgIdList) {
            SyncFuture syncFuture = BrokerServerSyncFutureManager.get(msgId);
            try {
                TcpMsg syncResponse = (TcpMsg) syncFuture.get(3, TimeUnit.SECONDS);
                if (syncResponse == null) {
                    logger.warn("{} slave broker push msg ack failed", msgId);
                }
            } catch (InterruptedException e) {
                logger.error("slave sync error is:", e);
            } catch (ExecutionException e) {
                logger.error("slave sync error is:", e);
            } catch (TimeoutException e) {
                logger.error("slave sync error is:", e);
            } catch (Exception e) {
                logger.error("slave sync unKnow error is:", e);
            }
        }
        logger.info("all slave node sync");
    }

    private void masterAsyncSlaveBroker(PushMsgEvent event) {
        // 主节点给所有的slave发送PushMsgEvent
        for (ChannelHandlerContext channelHandlerContext : CommonCache.getSlaveChannelMap().values()) {
            TcpMsg tcpMsg = new TcpMsg(BrokerEventCode.PUSH_MSG.getCode(), JSON.toJSONBytes(event.getMessageDTO()));
            channelHandlerContext.writeAndFlush(tcpMsg);
        }
    }

    private void singleModeSyncToBroker() {
    }

    private void singleModeSyncToBroker(PushMsgEvent event) {
        MessageDTO messageDTO = event.getMessageDTO();
        SendMessageToBrokerResponseDTO sendMessageToBrokerResponseDTO = new SendMessageToBrokerResponseDTO();
        sendMessageToBrokerResponseDTO.setStatus(SendMessageToBrokerResponseStatus.SUCCESS.getCode());
        sendMessageToBrokerResponseDTO.setMsgId(messageDTO.getMsgId());
        TcpMsg responseMsg = new TcpMsg(BrokerResponseCode.SEND_MSG_RESP.getCode(), JSON.toJSONBytes(sendMessageToBrokerResponseDTO));
        event.getChannelHandlerContext().writeAndFlush(responseMsg);
    }
}
