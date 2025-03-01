package org.tiny.mq.producer;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.common.codec.TcpMessage;
import org.tiny.mq.common.dto.*;
import org.tiny.mq.common.enums.*;
import org.tiny.mq.common.remote.BrokerNettyRemoteClient;
import org.tiny.mq.common.remote.NameServerNettyClient;
import org.tiny.mq.common.utils.AssertUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DefaultProducer implements Producer {
    private String nameserverIp;
    private Integer nameserverPort;
    private String nameserverUser;
    private String nameserverPassword;

    private NameServerNettyClient nameServerNettyClient;
    private List<String> brokerAddressList;
    private Map<String, BrokerNettyRemoteClient> brokerNettyRemoteClientMap = new ConcurrentHashMap<>();
    private static Logger logger = LoggerFactory.getLogger(DefaultProducer.class);

    public DefaultProducer() {

    }

    public List<String> getBrokerAddressList() {
        return brokerAddressList;
    }

    public void setBrokerAddressList(List<String> brokerAddressList) {
        this.brokerAddressList = brokerAddressList;
    }

    public Map<String, BrokerNettyRemoteClient> getBrokerNettyRemoteClientMap() {
        return brokerNettyRemoteClientMap;
    }

    public void setBrokerNettyRemoteClientMap(Map<String, BrokerNettyRemoteClient> brokerNettyRemoteClientMap) {
        this.brokerNettyRemoteClientMap = brokerNettyRemoteClientMap;
    }

    public DefaultProducer(String nameserverIp, Integer nameserverPort, String nameserverUser, String nameserverPassword) {
        this.nameserverIp = nameserverIp;
        this.nameserverPort = nameserverPort;
        this.nameserverUser = nameserverUser;
        this.nameserverPassword = nameserverPassword;
    }

    public String getNameserverIp() {
        return nameserverIp;
    }

    public void setNameserverIp(String nameserverIp) {
        this.nameserverIp = nameserverIp;
    }

    public Integer getNameserverPort() {
        return nameserverPort;
    }

    public void setNameserverPort(Integer nameserverPort) {
        this.nameserverPort = nameserverPort;
    }

    public String getNameserverUser() {
        return nameserverUser;
    }

    public void setNameserverUser(String nameserverUser) {
        this.nameserverUser = nameserverUser;
    }

    public String getNameserverPassword() {
        return nameserverPassword;
    }

    public void setNameserverPassword(String nameserverPassword) {
        this.nameserverPassword = nameserverPassword;
    }

    public void start() {
        nameServerNettyClient = new NameServerNettyClient(nameserverIp, nameserverPort);
        nameServerNettyClient.buildConnection();
        boolean isRegistrySuccess = this.doRegistry();
        if (isRegistrySuccess) {
            startHeartBeatTask();
            fetchBrokerAddress();
            connectBroker();
        }
    }

    public boolean doRegistry() {
        String msgId = UUID.randomUUID().toString();
        ServiceRegistryReqDTO serviceRegistryReqDTO = new ServiceRegistryReqDTO();
        serviceRegistryReqDTO.setMsgId(msgId);
        serviceRegistryReqDTO.setRegistryType(RegistryTypeEnum.PRODUCER.getType());
        serviceRegistryReqDTO.setUser(nameserverUser);
        serviceRegistryReqDTO.setPassword(nameserverPassword);
        TcpMessage tcpMessage = new TcpMessage(NameServerEventCode.REGISTRY.getCode(), JSON.toJSONBytes(serviceRegistryReqDTO));
        TcpMessage registryResponse = nameServerNettyClient.sendSyncMessage(tcpMessage, msgId);
        if (NameServerResponseCode.REGISTRY_SUCCESS.getCode() == registryResponse.getCode()) {
            logger.debug("registry success to nameserver {}:{}", nameserverIp, nameserverPort);
            return true;
        } else {
            logger.debug("registry failed to nameserver {}:{}", nameserverIp, nameserverPort);
            return false;
        }
    }

    private void heartBeatTask() {
        new Thread(new Thread(() -> {
            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(3);
                    logger.info("client heat beat");
                    HeartBeatDTO heartBeatDTO = new HeartBeatDTO();
                    String msgId = UUID.randomUUID().toString();
                    heartBeatDTO.setMsgId(msgId);
                    TcpMessage tcpMessage = new TcpMessage(NameServerEventCode.HEART_BEAT.getCode(), JSON.toJSONBytes(heartBeatDTO));
                    nameServerNettyClient.sendSyncMessage(tcpMessage, msgId);
                    logger.info("heat beat response");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "client heart beat task")).start();
    }

    private void startHeartBeatTask() {
        heartBeatTask();
    }

    public void fetchBrokerAddress() {
        String msgId = UUID.randomUUID().toString();
        PullBrokerIpReqDTO pullBrokerIpDTO = new PullBrokerIpReqDTO();
        pullBrokerIpDTO.setRole("single");
        pullBrokerIpDTO.setMsgId(msgId);
        TcpMessage tcpMessage = new TcpMessage(NameServerEventCode.PULL_BROKER_IP_LIST.getCode(), JSON.toJSONBytes(pullBrokerIpDTO));
        TcpMessage pullBrokerResponse = nameServerNettyClient.sendSyncMessage(tcpMessage, msgId);
        PullBrokerIpRespDTO pullBrokerIpRespDTO = JSON.parseObject(pullBrokerResponse.getBody(), PullBrokerIpRespDTO.class);
        this.setBrokerAddressList(pullBrokerIpRespDTO.getAddressList());
        logger.info("fetch Broker Address:{}", pullBrokerIpRespDTO.getAddressList());
    }

    private void connectBroker() {
        AssertUtils.isNotEmpty(this.getBrokerAddressList(), "broker地址不能为空");
        for (String brokerIp : brokerAddressList) {
            String[] brokerAddressArr = brokerIp.split(":");
            BrokerNettyRemoteClient brokerNettyRemoteClient = new BrokerNettyRemoteClient(brokerAddressArr[0], Integer.valueOf(brokerAddressArr[1]));
            brokerNettyRemoteClient.buildConnection();
            this.getBrokerNettyRemoteClientMap().put(brokerIp, brokerNettyRemoteClient);
            logger.info("success connect to broker:{}", brokerIp);
        }
    }

    @Override
    public SendResult sendMessage(MessageDTO message) {
        BrokerNettyRemoteClient remoteBrokerClient = this.getBrokerNettyRemoteClientMap().values().stream().collect(Collectors.toList()).get(0);
        String msgId = UUID.randomUUID().toString();
        message.setMsgId(msgId);
        message.setSendWay(MessageSendWayEnum.SYNC.getCode());
        TcpMessage tcpMessage = new TcpMessage(BrokerEventCode.PUSH_MSG.getCode(), JSON.toJSONBytes(message));
        TcpMessage response = remoteBrokerClient.sendSyncMessage(tcpMessage, msgId);
        SendMessageToBrokerRespDTO sendMessageToBrokerRespDTO = JSON.parseObject(response.getBody(), SendMessageToBrokerRespDTO.class);
        int status = sendMessageToBrokerRespDTO.getStatus();
        SendResult sendResult = new SendResult();
        if (status == SendMessageToBrokerRespStatusEnum.SUCCESS.getCode()) {
            sendResult.setSendStatus(SendStatus.SUCCESS);
        } else if (status == SendMessageToBrokerRespStatusEnum.FAIL.getCode()) {
            sendResult.setSendStatus(SendStatus.FAIL);
        }
        return sendResult;
    }

    @Override
    public void sendAsyncMessage(MessageDTO message) {
        BrokerNettyRemoteClient remoteBrokerClient = this.getBrokerNettyRemoteClientMap().values().stream().collect(Collectors.toList()).get(0);
        String msgId = UUID.randomUUID().toString();
        message.setMsgId(msgId);
        message.setSendWay(MessageSendWayEnum.ASYNC.getCode());
        TcpMessage tcpMessage = new TcpMessage(BrokerEventCode.PUSH_MSG.getCode(), JSON.toJSONBytes(message));
        remoteBrokerClient.sendAsyncMsg(tcpMessage);
    }
}
