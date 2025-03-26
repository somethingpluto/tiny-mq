package org.tiny.mq.consumer;

import com.alibaba.fastjson2.JSON;
import org.tiny.mq.common.coder.TcpMsg;
import org.tiny.mq.common.dto.CreateTopicReqDTO;
import org.tiny.mq.common.enums.BrokerEventCode;
import org.tiny.mq.common.remote.BrokerNettyRemoteClient;
import org.tiny.mq.common.remote.BrokerRemoteRespHandler;

import java.util.UUID;

public class CreateTopicCommand {

    public static void main(String[] args) {
        BrokerNettyRemoteClient brokerNettyRemoteClient = new BrokerNettyRemoteClient("127.0.0.1", 8990);
        brokerNettyRemoteClient.buildConnection(new BrokerRemoteRespHandler());
        CreateTopicReqDTO createTopicReqDTO = new CreateTopicReqDTO();
        String msgId = UUID.randomUUID().toString();
        createTopicReqDTO.setMsgId(msgId);
        createTopicReqDTO.setTopicName("retry");
        createTopicReqDTO.setQueueSize(1);
        brokerNettyRemoteClient.sendAsyncMsg(new TcpMsg(BrokerEventCode.CREATE_TOPIC.getCode(), JSON.toJSONBytes(createTopicReqDTO)));
    }
}
