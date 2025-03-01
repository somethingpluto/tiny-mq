package org.tiny.mq.nameserver.eventbus.listener;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.common.codec.TcpMessage;
import org.tiny.mq.common.dto.PullBrokerIpRespDTO;
import org.tiny.mq.common.enums.NameServerResponseCode;
import org.tiny.mq.common.enums.RegistryTypeEnum;
import org.tiny.mq.common.eventbus.Listener;
import org.tiny.mq.nameserver.config.GlobalConfig;
import org.tiny.mq.nameserver.eventbus.event.PullBrokerIPListEvent;
import org.tiny.mq.nameserver.store.ServiceInstance;

import java.util.ArrayList;
import java.util.Map;

public class PullBrokerIpListener implements Listener<PullBrokerIPListEvent> {
    private static final Logger logger = LoggerFactory.getLogger(PullBrokerIpListener.class);

    @Override
    public void onReceive(PullBrokerIPListEvent event) throws IllegalAccessException {
        logger.info("[EVENT][Pull Broker Ip List]:{}", event);
        String pullRole = event.getRole();
        PullBrokerIpRespDTO pullBrokerIpRespDTO = new PullBrokerIpRespDTO();
        ArrayList<String> brokerIPList = new ArrayList<>();
        Map<String, ServiceInstance> serviceInstanceMap = GlobalConfig.getServiceInstanceManager().getServiceInstanceMap();
        for (String reqId : serviceInstanceMap.keySet()) {
            ServiceInstance serviceInstance = serviceInstanceMap.get(reqId);
            if (serviceInstance.getRegistryType().equals(RegistryTypeEnum.BROKER.getType())) {
                Map<String, String> brokerAttrs = serviceInstance.getAttrs();
                String role = brokerAttrs.get("role");
                brokerIPList.add(serviceInstance.getIp() + ":" + serviceInstance.getPort());

//                if (PullBrokerIpRoleEnum.MASTER.getCode().equals(pullRole)
//                        && PullBrokerIpRoleEnum.MASTER.getCode().equals(role)) {
//                    brokerIPList.add(serviceInstance.getIp() + ":" + serviceInstance.getPort());
//                } else if (PullBrokerIpRoleEnum.SLAVE.getCode().equals(pullRole)
//                        && PullBrokerIpRoleEnum.SLAVE.getCode().equals(role)) {
//                    brokerIPList.add(serviceInstance.getIp() + ":" + serviceInstance.getPort());
//                } else if (PullBrokerIpRoleEnum.SINGLE.getCode().equals(pullRole)
//                        && PullBrokerIpRoleEnum.SINGLE.getCode().equals(role)) {
//                    brokerIPList.add(serviceInstance.getIp() + ":" + serviceInstance.getPort());
//
//                }
            }
        }
        pullBrokerIpRespDTO.setAddressList(brokerIPList);
        pullBrokerIpRespDTO.setMsgId(event.getMsgId());
        TcpMessage message = new TcpMessage(NameServerResponseCode.PULL_BROKER_ADDRESS_SUCCESS.getCode(), JSON.toJSONBytes(pullBrokerIpRespDTO));
        event.getChannelHandlerContext().writeAndFlush(message);
    }
}
