package org.tiny.mq.consumer;

import com.alibaba.fastjson2.JSON;
import org.tiny.mq.common.coder.TcpMsg;
import org.tiny.mq.common.dto.CreateTopicReqDTO;
import org.tiny.mq.common.enums.BrokerEventCode;
import org.tiny.mq.common.remote.BrokerNettyRemoteClient;

public class CreateTopicCommand {

    public static void main(String[] args) {
        BrokerNettyRemoteClient brokerNettyRemoteClient = new BrokerNettyRemoteClient("127.0.0.1", 8990);
        brokerNettyRemoteClient.buildConnection();
        CreateTopicReqDTO createTopicReqDTO = new CreateTopicReqDTO();
        createTopicReqDTO.setTopicName("detail_info");
        createTopicReqDTO.setQueueSize(10);
        brokerNettyRemoteClient.sendAsyncMsg(new TcpMsg(BrokerEventCode.CREATE_TOPIC.getCode(), JSON.toJSONBytes(createTopicReqDTO)));
    }
}
