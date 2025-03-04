package org.tiny.mq.consumer;

import com.alibaba.fastjson.JSON;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.common.codec.TcpMessage;
import org.tiny.mq.common.dto.*;
import org.tiny.mq.common.enums.*;
import org.tiny.mq.common.remote.BrokerNettyRemoteClient;
import org.tiny.mq.common.remote.NameServerNettyClient;
import org.tiny.mq.common.utils.AssertUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class DefaultConsumer {
    private final Logger logger = LoggerFactory.getLogger(DefaultConsumer.class);
    private final static int EACH_BATCH_PULL_MESSAGE_INTER = 100;// 从Broker拉取信息时间间隔 默认没100ms拉一次
    private final static int EACH_BATCH_PULL_MESSAGE_WHEN_NO_MESSAGE = 1000;// 当Broker没信息时 每隔1s拉一次

    private String nameserverIP;
    private Integer nameserverPort;
    private String nameserverUser;
    private String nameserverPassword;
    private String topic;
    private String consumerGroup;
    private String brokerRole = "single";
    private Integer queueId;
    private Integer batchSize;
    private NameServerNettyClient nameServerNettyClient;
    private List<String> brokerAddressList;
    private MessageConsumeListener messageConsumeListener;
    private Map<String, BrokerNettyRemoteClient> brokerNettyRemoteClientMap = new ConcurrentHashMap<>();
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    public void start() {
        this.nameServerNettyClient = new NameServerNettyClient(nameserverIP, nameserverPort);
        this.nameServerNettyClient.buildConnection();
        boolean isRegistrySuccess = this.doRegistry();
        if (isRegistrySuccess) {
            heartBeatTask();
            fetchBrokerAddress();
            connectBroker();
            startConsumeMsgTask();
        }
    }

    private void startConsumeMsgTask() {
        Thread consumeThread = new Thread(() -> {
            if ("single".equals(getBrokerRole())) {
                while (true) {
                    try {
                        // 获取默认的消费broker
                        String defaultBrokerAddress = brokerAddressList.get(0);
                        BrokerNettyRemoteClient brokerNettyRemoteClient = this.getBrokerNettyRemoteClientMap().get(defaultBrokerAddress);
                        // 从broker 拉取信息
                        String msgId = UUID.randomUUID().toString();
                        ConsumeMsgReqDTO consumeMsgReqDTO = new ConsumeMsgReqDTO();
                        consumeMsgReqDTO.setMsgId(msgId);
                        consumeMsgReqDTO.setConsumerGroup(consumerGroup);
                        consumeMsgReqDTO.setTopic(topic);
                        consumeMsgReqDTO.setBatchSize(batchSize);
                        TcpMessage pullReqMessage = new TcpMessage(BrokerEventCode.CONSUME_MSG.getCode(), JSON.toJSONBytes(consumeMsgReqDTO));
                        TcpMessage pullRespMessage = brokerNettyRemoteClient.sendSyncMessage(pullReqMessage, msgId);
                        ConsumeMsgBaseRespDTO consumeMsgBaseRespDTO = JSON.parseObject(pullRespMessage.getBody(), ConsumeMsgBaseRespDTO.class);
                        List<ConsumeMsgRespDTO> consumeMsgRespDTOS = consumeMsgBaseRespDTO.getConsumeMsgRespDTOList();
                        // 检查broker是否包含信息
                        boolean brokerHasData = false;
                        if (CollectionUtils.isNotEmpty(consumeMsgRespDTOS)) {

                            for (ConsumeMsgRespDTO consumeMsgRespDTO : consumeMsgRespDTOS) {
                                List<byte[]> commitLogContentList = consumeMsgRespDTO.getCommitLogContentList();
                                if (CollectionUtils.isEmpty(commitLogContentList)) {
                                    continue;
                                }
                                ArrayList<ConsumeMessageDTO> consumeMessageDTOS = new ArrayList<>();
                                for (byte[] bytes : commitLogContentList) {
                                    ConsumeMessageDTO consumeMessageDTO = new ConsumeMessageDTO();
                                    consumeMessageDTO.setBody(bytes);
                                    consumeMessageDTO.setQueueId(consumeMsgRespDTO.getQueueId());
                                    consumeMessageDTOS.add(consumeMessageDTO);
                                }
                                brokerHasData = true;
                                ConsumeResult consumeResult = messageConsumeListener.consume(consumeMessageDTOS);
                                // 消费成功
                                if (consumeResult.getConsumeResultStatus() == ConsumeResultStatus.CONSUME_SUCCESS.getCode()) {
                                    String ackMessageId = UUID.randomUUID().toString();
                                    ConsumeMsgAckReqDTO consumeMsgAckReqDTO = new ConsumeMsgAckReqDTO();
                                    consumeMsgAckReqDTO.setMsgId(ackMessageId);
                                    consumeMsgAckReqDTO.setAckCount(this.getBatchSize());
                                    consumeMsgAckReqDTO.setQueueId(consumeMsgRespDTO.getQueueId());
                                    consumeMsgAckReqDTO.setConsumerGroup(this.consumerGroup);
                                    consumeMsgAckReqDTO.setTopic(this.topic);
                                    TcpMessage ackReqMessage = new TcpMessage(BrokerEventCode.CONSUME_SUCCESS_MSG.getCode(), JSON.toJSONBytes(consumeMsgAckReqDTO));
                                    TcpMessage ackMessageResponse = brokerNettyRemoteClient.sendSyncMessage(ackReqMessage, ackMessageId);
                                    ConsumeMsgAckRespDTO consumeMsgAckRespDTO = JSON.parseObject(ackMessageResponse.getBody(), ConsumeMsgAckRespDTO.class);
                                    if (AckStatusEnum.SUCCESS.getCode() == consumeMsgAckRespDTO.getAckStatus()) {
                                        logger.info("consume ack success!");
                                    } else {
                                        logger.error("consume ack fail!");
                                    }
                                }
                            }
                        }
                        if (brokerHasData) {
                            TimeUnit.MILLISECONDS.sleep(EACH_BATCH_PULL_MESSAGE_INTER);
                        } else {
                            TimeUnit.MILLISECONDS.sleep(EACH_BATCH_PULL_MESSAGE_WHEN_NO_MESSAGE);
                        }
                    } catch (Exception e) {
                        logger.info("consume has error: ", e);
                    }
                }
            }
        });
        consumeThread.setName("consumer");
        consumeThread.start();
    }


    private boolean doRegistry() {
        String msgId = UUID.randomUUID().toString();
        ServiceRegistryReqDTO serviceRegistryReqDTO = new ServiceRegistryReqDTO();
        serviceRegistryReqDTO.setMsgId(msgId);
        serviceRegistryReqDTO.setUser(nameserverUser);
        serviceRegistryReqDTO.setPassword(nameserverPassword);
        serviceRegistryReqDTO.setRegistryType(RegistryTypeEnum.CONSUMER.getType());
        TcpMessage tcpMessage = new TcpMessage(NameServerEventCode.REGISTRY.getCode(), JSON.toJSONBytes(serviceRegistryReqDTO));
        TcpMessage registryResponse = nameServerNettyClient.sendSyncMessage(tcpMessage, msgId);
        if (NameServerResponseCode.REGISTRY_SUCCESS.getCode() == registryResponse.getCode()) {
            return true;
        } else {
            logger.info("registry failed");
            return false;
        }
    }

    private void heartBeatTask() {
        new Thread(new Thread(() -> {
            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(3);
                    logger.info("consumer heat beat");
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
        }
    }


    public String getNameserverIP() {
        return nameserverIP;
    }

    public void setNameserverIP(String nameserverIP) {
        this.nameserverIP = nameserverIP;
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

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public String getBrokerRole() {
        return brokerRole;
    }

    public void setBrokerRole(String brokerRole) {
        this.brokerRole = brokerRole;
    }

    public Integer getQueueId() {
        return queueId;
    }

    public void setQueueId(Integer queueId) {
        this.queueId = queueId;
    }

    public Integer getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    public NameServerNettyClient getNameServerNettyClient() {
        return nameServerNettyClient;
    }

    public void setNameServerNettyClient(NameServerNettyClient nameServerNettyClient) {
        this.nameServerNettyClient = nameServerNettyClient;
    }

    public List<String> getBrokerAddressList() {
        return brokerAddressList;
    }

    public void setBrokerAddressList(List<String> brokerAddressList) {
        this.brokerAddressList = brokerAddressList;
    }

    public MessageConsumeListener getMessageConsumeListener() {
        return messageConsumeListener;
    }

    public void setMessageConsumeListener(MessageConsumeListener messageConsumeListener) {
        this.messageConsumeListener = messageConsumeListener;
    }

    public Map<String, BrokerNettyRemoteClient> getBrokerNettyRemoteClientMap() {
        return brokerNettyRemoteClientMap;
    }

    public void setBrokerNettyRemoteClientMap(Map<String, BrokerNettyRemoteClient> brokerNettyRemoteClientMap) {
        this.brokerNettyRemoteClientMap = brokerNettyRemoteClientMap;
    }

    public CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }

    public void setCountDownLatch(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }
}
