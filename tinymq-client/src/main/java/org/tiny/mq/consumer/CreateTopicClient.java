package org.tiny.mq.consumer;

import com.alibaba.fastjson.JSON;
import org.tiny.mq.common.coder.TcpMsg;
import org.tiny.mq.common.dto.CreateTopicReqDTO;
import org.tiny.mq.common.enums.BrokerEventCode;
import org.tiny.mq.common.event.EventBus;
import org.tiny.mq.common.remote.BrokerNettyRemoteClient;
import org.tiny.mq.netty.RemoteBrokerRespHandler;

import java.util.UUID;

public class CreateTopicClient {
    public void createTopic(String topic, String brokerAddress) {
        String[] brokerAddr = brokerAddress.split(":");
        String ip = brokerAddr[0];
        Integer port = Integer.valueOf(brokerAddr[1]);
        BrokerNettyRemoteClient brokerNettyRemoteClient = new BrokerNettyRemoteClient(ip, port);
        brokerNettyRemoteClient.buildConnection(new RemoteBrokerRespHandler(new EventBus("mq-client-eventbus")));
        CreateTopicReqDTO createTopicReqDTO = new CreateTopicReqDTO();
        createTopicReqDTO.setTopicName(topic);
        createTopicReqDTO.setQueueSize(3);
        TcpMsg respMsg = brokerNettyRemoteClient.sendSyncMsg(new TcpMsg(BrokerEventCode.CREATE_TOPIC.getCode(), JSON.toJSONBytes(createTopicReqDTO)), UUID.randomUUID().toString());
        System.out.println("resp:" + JSON.toJSONString(respMsg));
        brokerNettyRemoteClient.close();
    }
}
