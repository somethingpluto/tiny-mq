package org.tiny.mq.consumer;

import com.alibaba.fastjson.JSON;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.common.coder.TcpMsg;
import org.tiny.mq.common.dto.*;
import org.tiny.mq.common.enums.*;
import org.tiny.mq.common.remote.BrokerNettyRemoteClient;
import org.tiny.mq.common.remote.BrokerRemoteRespHandler;
import org.tiny.mq.common.remote.NameServerNettyRemoteClient;
import org.tiny.mq.common.utils.AssertUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


public class DefaultMqConsumer {

    private final Logger logger = LoggerFactory.getLogger(DefaultMqConsumer.class);
    private final static int EACH_BATCH_PULL_MSG_INTER = 100; //如果broker有数据，每间隔100ms拉一批
    private final static int EACH_BATCH_PULL_MSG_INTER_WHEN_NO_MSG = 1000; //如果broker无数据，每间隔1s拉一批

    private String nsIp;
    private Integer nsPort;
    private String nsUser;
    private String nsPwd;
    private String topic;
    private String consumeGroup;
    private String brokerRole = "single";
    private Integer queueId;
    private Integer batchSize;

    private String brokerClusterGroup;
    private NameServerNettyRemoteClient nameServerNettyRemoteClient;
    private List<String> brokerAddressList;
    private MessageConsumeListener messageConsumeListener;
    private Map<String, BrokerNettyRemoteClient> brokerNettyRemoteClientMap = new ConcurrentHashMap<>();
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    public void start() throws InterruptedException {
        nameServerNettyRemoteClient = new NameServerNettyRemoteClient(nsIp, nsPort);
        nameServerNettyRemoteClient.buildConnection();
        boolean isRegistrySuccess = this.doRegistry();
        if (isRegistrySuccess) {
            this.startHeartBeatTask();
            this.fetchBrokerAddress();
            this.connectBroker();
            this.startConsumeMsgTask();
            countDownLatch.await();
        }
    }

    /**
     * 开启消费数据任务
     */
    private void startConsumeMsgTask() {
        Thread consumeTask = new Thread(() -> {
            //不知道对应topic位于哪个Broker节点 todo
            if ("single".equals(getBrokerRole())) {
                while (true) {
                    try {
                        String defaultBrokerAddress = brokerAddressList.get(0);
                        String msgId = UUID.randomUUID().toString();
                        BrokerNettyRemoteClient brokerNettyRemoteClient = this.getBrokerNettyRemoteClientMap().get(defaultBrokerAddress);
                        ConsumeMsgReqDTO consumeMsgReqDTO = new ConsumeMsgReqDTO();
                        consumeMsgReqDTO.setMsgId(msgId);
                        consumeMsgReqDTO.setConsumeGroup(consumeGroup);
                        consumeMsgReqDTO.setBatchSize(batchSize);
                        consumeMsgReqDTO.setTopic(topic);
                        TcpMsg pullReqMsg = new TcpMsg(BrokerEventCode.CONSUME_MSG.getCode(), JSON.toJSONBytes(consumeMsgReqDTO));
                        TcpMsg pullMsgResp = brokerNettyRemoteClient.sendSyncMsg(pullReqMsg, msgId);
                        ConsumeMsgBaseRespDTO consumeMsgBaseRespDTO = JSON.parseObject(pullMsgResp.getBody(), ConsumeMsgBaseRespDTO.class);
                        List<ConsumeMsgRespDTO> consumeMsgRespDTOS = consumeMsgBaseRespDTO.getConsumeMsgRespDTOList();
                        boolean brokerHasData = false;
                        if (CollectionUtils.isNotEmpty(consumeMsgRespDTOS)) {
                            for (ConsumeMsgRespDTO consumeMsgRespDTO : consumeMsgRespDTOS) {
                                List<byte[]> commitLogBodyList = consumeMsgRespDTO.getCommitLogContentList();
                                if (CollectionUtils.isEmpty(commitLogBodyList)) {
                                    continue;
                                }
                                List<ConsumeMessage> consumeMessages = new ArrayList<>();
                                for (byte[] bytes : commitLogBodyList) {
                                    ConsumeMessage consumeMessage = new ConsumeMessage();
                                    consumeMessage.setBody(bytes);
                                    consumeMessage.setQueueId(consumeMsgRespDTO.getQueueId());
                                    consumeMessages.add(consumeMessage);
                                }
                                brokerHasData = true;
                                ConsumeResult consumeResult = messageConsumeListener.consume(consumeMessages);
                                //消费成功，发送ack响应
                                if (consumeResult.getConsumeResultStatus() == ConsumeResultStatus.CONSUME_SUCCESS.getCode()) {
                                    String ackMsgId = UUID.randomUUID().toString();
                                    ConsumeMsgAckReqDTO consumeMsgAckReqDTO = new ConsumeMsgAckReqDTO();
                                    consumeMsgAckReqDTO.setAckCount(this.getBatchSize());
                                    consumeMsgAckReqDTO.setConsumeGroup(this.getConsumeGroup());
                                    consumeMsgAckReqDTO.setTopic(this.getTopic());
                                    consumeMsgAckReqDTO.setQueueId(consumeMsgRespDTO.getQueueId());
                                    consumeMsgAckReqDTO.setMsgId(ackMsgId);
                                    TcpMsg ackReq = new TcpMsg(BrokerEventCode.CONSUME_SUCCESS_MSG.getCode(), JSON.toJSONBytes(consumeMsgAckReqDTO));
                                    TcpMsg ackResponse = brokerNettyRemoteClient.sendSyncMsg(ackReq, ackMsgId);
                                    ConsumeMsgAckRespDTO consumeMsgAckRespDTO = JSON.parseObject(ackResponse.getBody(), ConsumeMsgAckRespDTO.class);
                                    if (AckStatus.SUCCESS.getCode() == consumeMsgAckRespDTO.getAckStatus()) {
                                        logger.info("consume ack success!");
                                    } else {
                                        logger.error("consume ack fail!");
                                    }
                                }
                            }
                        }
                        if (brokerHasData) {
                            TimeUnit.MILLISECONDS.sleep(EACH_BATCH_PULL_MSG_INTER);
                        } else {
                            TimeUnit.MILLISECONDS.sleep(EACH_BATCH_PULL_MSG_INTER_WHEN_NO_MSG);
                        }
                    } catch (Exception e) {
                        logger.error("consume has error:", e);
                    }
                }
            }
        });
        consumeTask.setName("consume-msg-task");
        consumeTask.start();
    }

    /**
     * 连接broker程序
     */
    private void connectBroker() {
        AssertUtils.isNotEmpty(this.getBrokerAddressList(), "broker地址不能为空");
        for (String brokerIp : brokerAddressList) {
            String[] brokerAddressArr = brokerIp.split(":");
            BrokerNettyRemoteClient brokerNettyRemoteClient = new BrokerNettyRemoteClient(brokerAddressArr[0], Integer.valueOf(brokerAddressArr[1]));
            brokerNettyRemoteClient.buildConnection(new BrokerRemoteRespHandler());
            this.getBrokerNettyRemoteClientMap().put(brokerIp, brokerNettyRemoteClient);
        }
    }


    /**
     * 开启注册
     *
     * @return
     */
    private boolean doRegistry() {
        String registryMsgId = UUID.randomUUID().toString();
        ServiceRegistryReqDTO serviceRegistryReqDTO = new ServiceRegistryReqDTO();
        serviceRegistryReqDTO.setMsgId(registryMsgId);
        serviceRegistryReqDTO.setUser(nsUser);
        serviceRegistryReqDTO.setPassword(nsPwd);
        serviceRegistryReqDTO.setRegistryType(RegistryTypeEnum.CONSUMER.getCode());
        TcpMsg tcpMsg = new TcpMsg(NameServerEventCode.REGISTRY.getCode(), JSON.toJSONBytes(serviceRegistryReqDTO));
        TcpMsg registryResponse = nameServerNettyRemoteClient.sendSyncMsg(tcpMsg, registryMsgId);
        if (NameServerResponseCode.REGISTRY_SUCCESS.getCode() == registryResponse.getCode()) {
            return true;
        } else {
            logger.error("注册账号失败");
            return false;
        }
    }

    /**
     * 启动心跳任务
     */
    private void startHeartBeatTask() {
        Thread heartBeatTask = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        TimeUnit.SECONDS.sleep(3);
                        logger.info("执行心跳数据发送");
                        String heartBeatMsgId = UUID.randomUUID().toString();
                        HeartBeatDTO heartBeatDTO = new HeartBeatDTO();
                        heartBeatDTO.setMsgId(heartBeatMsgId);
                        TcpMsg heartBeatResponse = nameServerNettyRemoteClient.sendSyncMsg(new TcpMsg(NameServerEventCode.HEART_BEAT.getCode(), JSON.toJSONBytes(heartBeatDTO)), heartBeatMsgId);
                        logger.info("heart beat response data is :{}", JSON.parseObject(heartBeatResponse.getBody()));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, "heart-beat-task");
        heartBeatTask.start();
    }

    /**
     * 拉broker地址
     * <p>
     * 主从架构下 从节点拉取数据 主节点拉去数据
     */
    public void fetchBrokerAddress() {
        String fetchBrokerAddressMsgId = UUID.randomUUID().toString();
        PullBrokerIpDTO pullBrokerIpDTO = new PullBrokerIpDTO();
        if (getBrokerClusterGroup() != null) {
            pullBrokerIpDTO.setBrokerClusterGroup(brokerClusterGroup);
        } else {
            pullBrokerIpDTO.setRole(brokerRole);
        }
        pullBrokerIpDTO.setMsgId(fetchBrokerAddressMsgId);
        TcpMsg heartBeatResponse = nameServerNettyRemoteClient.sendSyncMsg(new TcpMsg(NameServerEventCode.PULL_BROKER_IP_LIST.getCode(), JSON.toJSONBytes(pullBrokerIpDTO)), fetchBrokerAddressMsgId);
        //获取broker节点ip地址，并且缓存起来，可能由多个master-broker角色
        PullBrokerIpRespDTO pullBrokerIpRespDTO = JSON.parseObject(heartBeatResponse.getBody(), PullBrokerIpRespDTO.class);
        this.setBrokerAddressList(pullBrokerIpRespDTO.getAddressList());
        logger.info("fetch broker address:{}", this.getBrokerAddressList());
    }


    public String getBrokerClusterGroup() {
        return brokerClusterGroup;
    }

    public void setBrokerClusterGroup(String brokerClusterGroup) {
        this.brokerClusterGroup = brokerClusterGroup;
    }

    public String getBrokerRole() {
        return brokerRole;
    }

    public void setBrokerRole(String brokerRole) {
        this.brokerRole = brokerRole;
    }

    public Map<String, BrokerNettyRemoteClient> getBrokerNettyRemoteClientMap() {
        return brokerNettyRemoteClientMap;
    }

    public void setBrokerNettyRemoteClientMap(Map<String, BrokerNettyRemoteClient> brokerNettyRemoteClientMap) {
        this.brokerNettyRemoteClientMap = brokerNettyRemoteClientMap;
    }

    public MessageConsumeListener getMessageConsumeListener() {
        return messageConsumeListener;
    }

    public void setMessageConsumeListener(MessageConsumeListener messageConsumeListener) {
        this.messageConsumeListener = messageConsumeListener;
    }

    public NameServerNettyRemoteClient getNameServerNettyRemoteClient() {
        return nameServerNettyRemoteClient;
    }

    public void setNameServerNettyRemoteClient(NameServerNettyRemoteClient nameServerNettyRemoteClient) {
        this.nameServerNettyRemoteClient = nameServerNettyRemoteClient;
    }

    public List<String> getBrokerAddressList() {
        return brokerAddressList;
    }

    public void setBrokerAddressList(List<String> brokerAddressList) {
        this.brokerAddressList = brokerAddressList;
    }

    public String getNsIp() {
        return nsIp;
    }

    public void setNsIp(String nsIp) {
        this.nsIp = nsIp;
    }

    public Integer getNsPort() {
        return nsPort;
    }

    public void setNsPort(Integer nsPort) {
        this.nsPort = nsPort;
    }

    public String getNsUser() {
        return nsUser;
    }

    public void setNsUser(String nsUser) {
        this.nsUser = nsUser;
    }

    public String getNsPwd() {
        return nsPwd;
    }

    public void setNsPwd(String nsPwd) {
        this.nsPwd = nsPwd;
    }


    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getConsumeGroup() {
        return consumeGroup;
    }

    public void setConsumeGroup(String consumeGroup) {
        this.consumeGroup = consumeGroup;
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


}
