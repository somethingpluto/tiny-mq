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
import java.util.stream.Collectors;


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
    private List<String> masterAddressList;
    private List<String> slaveAddressList;
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
            this.startConsumeMsgTask();
            this.startRefreshBrokerAddressTask();
            countDownLatch.await();
        }
    }

    /**
     * 开启消费数据任务
     */
    private void startConsumeMsgTask() {
        Thread consumeTask = new Thread(() -> {
            while (true) {
                try {
                    List<String> brokerNodeAddressList = new ArrayList<>();
                    String currentRole = getBrokerRole();
                    if (currentRole.equals(BrokerRegistryEnum.SINGLE.getDesc())) {
                        brokerNodeAddressList = this.getBrokerAddressList();
                    } else if (currentRole.equals(BrokerRegistryEnum.MASTER.getDesc())) {
                        brokerNodeAddressList = this.getMasterAddressList();
                    } else if (currentRole.equals(BrokerRegistryEnum.SLAVE.getDesc())) {
                        brokerNodeAddressList = this.getSlaveAddressList();
                    }
                    if (CollectionUtils.isEmpty(brokerNodeAddressList)) {
                        // 等一会
                        TimeUnit.MILLISECONDS.sleep(EACH_BATCH_PULL_MSG_INTER_WHEN_NO_MSG);
                        logger.warn("broker address is empty!");
                        continue;
                    }
                    for (String brokerAddress : brokerAddressList) {
                        String msgId = UUID.randomUUID().toString();
                        BrokerNettyRemoteClient brokerNettyRemoteClient = this.getBrokerNettyRemoteClientMap().get(brokerAddress);
                        ConsumeMsgReqDTO consumeMsgReqDTO = new ConsumeMsgReqDTO();
                        consumeMsgReqDTO.setMsgId(msgId);
                        consumeMsgReqDTO.setConsumeGroup(consumeGroup);
                        consumeMsgReqDTO.setTopic(topic);
                        consumeMsgReqDTO.setBatchSize(batchSize);
                        TcpMsg consumeReqMsg = new TcpMsg(BrokerEventCode.CONSUME_MSG.getCode(), JSON.toJSONBytes(consumeMsgReqDTO));
                        TcpMsg pullMsgResp = brokerNettyRemoteClient.sendSyncMsg(consumeReqMsg, msgId);
                        List<ConsumeMsgRespDTO> consumeMsgRespDTOS = null;
                        ConsumeMsgBaseRespDTO consumeMsgBaseRespDTO = JSON.parseObject(pullMsgResp.getBody(), ConsumeMsgBaseRespDTO.class);
                        if (consumeMsgBaseRespDTO != null) {
                            consumeMsgRespDTOS = consumeMsgBaseRespDTO.getConsumeMsgRespDTOList();
                        }
                        boolean brokerHasData = false;
                        if (CollectionUtils.isNotEmpty(consumeMsgRespDTOS)) {
                            for (ConsumeMsgRespDTO consumeMsgRespDTO : consumeMsgRespDTOS) {
                                List<ConsumeMsgCommitLogDTO> commitLogBodyList = consumeMsgRespDTO.getCommitLogContentList();
                                if (CollectionUtils.isEmpty(commitLogBodyList)) {
                                    continue;
                                }
                                List<ConsumeMessage> consumeMessages = new ArrayList<>();
                                for (ConsumeMsgCommitLogDTO consumeMsgCommitLogDTO : commitLogBodyList) {
                                    ConsumeMessage consumeMessage = new ConsumeMessage();
                                    consumeMessage.setConsumeMsgCommitLogDTO(consumeMsgCommitLogDTO);
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
                                } else if (consumeResult.getConsumeResultStatus() == ConsumeResultStatus.CONSUME_LATER.getCode()) {
                                    this.setQueueId(consumeMsgRespDTO.getQueueId());
                                    List<Long> commitLogOffsetList = commitLogBodyList.stream().map(ConsumeMsgCommitLogDTO::getCommitLogOffset).collect(Collectors.toList());
                                    List<Integer> commitLogMsgLengthList = commitLogBodyList.stream().map(commitLogDTO -> commitLogDTO.getBody().length).collect(Collectors.toList());
                                    String commitLogFileName = commitLogBodyList.get(0).getFileName();
                                    String consumeMsgLaterReqId = UUID.randomUUID().toString();
                                    ConsumeMsgRetryReqDTO consumeMsgRetryReqDTO = new ConsumeMsgRetryReqDTO();
                                    consumeMsgRetryReqDTO.setConsumerGroup(this.getConsumeGroup());
                                    consumeMsgRetryReqDTO.setTopic(this.getTopic());
                                    consumeMsgRetryReqDTO.setQueueId(consumeMsgRespDTO.getQueueId());
                                    consumeMsgRetryReqDTO.setMsgId(consumeMsgLaterReqId);
                                    consumeMsgRetryReqDTO.setCommitLogOffsetList(commitLogOffsetList);
                                    consumeMsgRetryReqDTO.setCommitLogMsgLengthList(commitLogMsgLengthList);
                                    consumeMsgRetryReqDTO.setRetryTime(1);
                                    consumeMsgRetryReqDTO.setCommitLogName(commitLogFileName);
                                    consumeMsgRetryReqDTO.setAckCount(this.getBatchSize());
                                    TcpMsg tcpMsg = new TcpMsg(BrokerEventCode.CONSUME_RETRY.getCode(), JSON.toJSONBytes(consumeMsgRetryReqDTO));
                                    TcpMsg resp = brokerNettyRemoteClient.sendSyncMsg(tcpMsg, consumeMsgLaterReqId);
                                    logger.info("consume later resp:{}", JSON.toJSONString(resp));
                                }
                            }
                        }
                        if (brokerHasData) {
                            TimeUnit.MILLISECONDS.sleep(EACH_BATCH_PULL_MSG_INTER);
                        } else {
                            TimeUnit.MILLISECONDS.sleep(EACH_BATCH_PULL_MSG_INTER_WHEN_NO_MSG);
                        }
                    }
                } catch (Exception e) {
                    logger.error("consume has error:", e);
                    try {
                        TimeUnit.MILLISECONDS.sleep(EACH_BATCH_PULL_MSG_INTER_WHEN_NO_MSG);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
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
        List<String> brokerAddressList = new ArrayList<>();
        String currentBrokerRole = this.getBrokerRole();
        if (currentBrokerRole.equals(BrokerRegistryEnum.SINGLE.getDesc())) { // 单机版本
            AssertUtils.isNotEmpty(this.getBrokerAddressList(), "broker address should not be empty");
            brokerAddressList = this.getBrokerAddressList();
        } else if (currentBrokerRole.equals(BrokerRegistryEnum.MASTER.getDesc())) { // 主从模式下的master
            AssertUtils.isNotEmpty(this.getBrokerAddressList(), "broker address should not be empty");
            brokerAddressList = this.getBrokerAddressList();
        } else if (currentBrokerRole.equals(BrokerRegistryEnum.SLAVE.getDesc())) { // 主从模式下的slave
            AssertUtils.isNotEmpty(this.getBrokerAddressList(), "broker address should not be empty");
            brokerAddressList = this.getBrokerAddressList();
        }
        ArrayList<BrokerNettyRemoteClient> newBrokerRemoteClientList = new ArrayList<>();
        // 遍历address list 看看有没有连接 没有则新建 有则保存
        for (String brokerAddress : brokerAddressList) {
            BrokerNettyRemoteClient brokerNettyRemoteClient = this.brokerNettyRemoteClientMap.get(brokerAddress);
            if (brokerNettyRemoteClient == null) { // 新的地址 要新建连接
                String[] brokerAddressInfo = brokerAddress.split(":");
                BrokerNettyRemoteClient newBrokerNettyRemoteClient = new BrokerNettyRemoteClient(brokerAddressInfo[0], Integer.valueOf(brokerAddressInfo[1]));
                newBrokerNettyRemoteClient.buildConnection(new BrokerRemoteRespHandler());
                newBrokerRemoteClientList.add(newBrokerNettyRemoteClient);
                continue;
            }
            if (brokerNettyRemoteClient.isChannelActive()) {
                newBrokerRemoteClientList.add(brokerNettyRemoteClient);
            } else { // 老连接不行了
                String[] brokerAddressInfo = brokerAddress.split(":");
                BrokerNettyRemoteClient newBrokerNettyRemoteClient = new BrokerNettyRemoteClient(brokerAddressInfo[0], Integer.valueOf(brokerAddressInfo[1]));
                newBrokerNettyRemoteClient.buildConnection(new BrokerRemoteRespHandler());
                newBrokerRemoteClientList.add(newBrokerNettyRemoteClient);
            }
        }
        // 对比新旧连接 对老链接需要优雅关闭
        List<String> finalBrokerAddressList = brokerAddressList;
        List<String> needCloseClint = this.getBrokerNettyRemoteClientMap().keySet().stream().filter(reqId -> !finalBrokerAddressList.contains(reqId)).collect(Collectors.toList());
        for (String reqId : needCloseClint) {
            this.getBrokerNettyRemoteClientMap().get(reqId).close();
            this.getBrokerNettyRemoteClientMap().remove(reqId);
        }
        this.setBrokerNettyRemoteClientMap(newBrokerRemoteClientList.stream().collect(Collectors.toMap(BrokerNettyRemoteClient::getBrokerReqId, item -> item)));
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
     * 拉broker地址
     * <p>
     * 主从架构下 从节点拉取数据 主节点拉去数据
     */
    public void fetchBrokerAddress() {
        String fetchBrokerAddressMsgId = UUID.randomUUID().toString();
        PullBrokerIpReqDTO pullBrokerIpDTO = new PullBrokerIpReqDTO();
        if (getBrokerClusterGroup() != null) {
            pullBrokerIpDTO.setBrokerClusterGroup(brokerClusterGroup);
        }
        pullBrokerIpDTO.setRole(brokerRole);
        pullBrokerIpDTO.setMsgId(fetchBrokerAddressMsgId);
        TcpMsg heartBeatResponse = nameServerNettyRemoteClient.sendSyncMsg(new TcpMsg(NameServerEventCode.PULL_BROKER_IP_LIST.getCode(), JSON.toJSONBytes(pullBrokerIpDTO)), fetchBrokerAddressMsgId);
        PullBrokerIpRespDTO pullBrokerIpRespDTO = JSON.parseObject(heartBeatResponse.getBody(), PullBrokerIpRespDTO.class);
        this.setBrokerAddressList(pullBrokerIpRespDTO.getAddressList());
        this.setMasterAddressList(pullBrokerIpRespDTO.getMasterList());
        this.setSlaveAddressList(pullBrokerIpRespDTO.getSlaveList());
        logger.info("fetch broker address:{}", this.getBrokerAddressList());
        this.connectBroker();
    }

    public void startRefreshBrokerAddressTask() {
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(3);
                    fetchBrokerAddress();
                } catch (Exception e) {
                    logger.error("refresh broker address job error:", e);
                }
            }
        });
        thread.setName("refresh-broker-address");
        thread.start();
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

    public List<String> getMasterAddressList() {
        return masterAddressList;
    }

    public void setMasterAddressList(List<String> masterAddressList) {
        this.masterAddressList = masterAddressList;
    }

    public List<String> getSlaveAddressList() {
        return slaveAddressList;
    }

    public void setSlaveAddressList(List<String> slaveAddressList) {
        this.slaveAddressList = slaveAddressList;
    }
}
