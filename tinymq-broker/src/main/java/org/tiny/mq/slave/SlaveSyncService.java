package org.tiny.mq.slave;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.common.coder.TcpMsg;
import org.tiny.mq.common.dto.StartSyncReqDTO;
import org.tiny.mq.common.enums.BrokerEventCode;
import org.tiny.mq.common.remote.BrokerNettyRemoteClient;

import java.util.UUID;

public class SlaveSyncService {
    private BrokerNettyRemoteClient brokerNettyRemoteClient;
    private static final Logger logger = LoggerFactory.getLogger(SlaveSyncService.class);

    public boolean connectToMasterBrokerNode(String address) {
        String[] addressInfo = address.split(":");
        String ip = addressInfo[0];
        Integer port = Integer.valueOf(addressInfo[1]);
        try {
            brokerNettyRemoteClient = new BrokerNettyRemoteClient(ip, port);
            brokerNettyRemoteClient.buildConnection();
        } catch (Exception e) {
            logger.error("error connect master broker", e);
        }
        return false;
    }

    public void sendStartSyncMsg() {
        StartSyncReqDTO startSyncReqDTO = new StartSyncReqDTO();
        startSyncReqDTO.setMsgId(UUID.randomUUID().toString());
        TcpMsg tcpMsg = new TcpMsg(BrokerEventCode.START_SYNC_MSG.getCode(), JSON.toJSONBytes(startSyncReqDTO));
        TcpMsg starSyncMsgResp = brokerNettyRemoteClient.sendSyncMsg(tcpMsg, startSyncReqDTO.getMsgId());
        logger.info("start sync message is:{}", JSON.toJSONString(starSyncMsgResp));
    }
}
