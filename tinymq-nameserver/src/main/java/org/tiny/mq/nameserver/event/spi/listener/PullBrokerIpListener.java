package org.tiny.mq.nameserver.event.spi.listener;

import com.alibaba.fastjson.JSON;
import org.tiny.mq.common.coder.TcpMsg;
import org.tiny.mq.common.dto.PullBrokerIpRespDTO;
import org.tiny.mq.common.enums.BrokerRegistryEnum;
import org.tiny.mq.common.enums.NameServerResponseCode;
import org.tiny.mq.common.enums.RegistryTypeEnum;
import org.tiny.mq.common.event.Listener;
import org.tiny.mq.nameserver.common.CommonCache;
import org.tiny.mq.nameserver.event.model.PullBrokerIpEvent;
import org.tiny.mq.nameserver.store.ServiceInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PullBrokerIpListener implements Listener<PullBrokerIpEvent> {

    @Override
    public void onReceive(PullBrokerIpEvent event) throws Exception {
        String pullRole = event.getRole();
        PullBrokerIpRespDTO pullBrokerIpRespDTO = new PullBrokerIpRespDTO();
        List<String> addressList = new ArrayList<>();
        List<String> masterAddressList = new ArrayList<>();
        List<String> slaveAddressList = new ArrayList<>();
        Map<String, ServiceInstance> serviceInstanceMap = CommonCache.getServiceInstanceManager().getServiceInstanceMap();
        for (String reqId : serviceInstanceMap.keySet()) {
            ServiceInstance serviceInstance = serviceInstanceMap.get(reqId);
            if (RegistryTypeEnum.BROKER.getCode().equals(serviceInstance.getRegistryType())) {
                Map<String, Object> brokerAttrs = serviceInstance.getAttrs();
                String group = (String) brokerAttrs.getOrDefault("group", "");
                //先命中集群组，再根据角色进行判断
                if (group.equals(event.getBrokerClusterGroup())) {
                    String itemRole = (String) brokerAttrs.get("role");
                    if (itemRole.equals(BrokerRegistryEnum.MASTER.getDesc()) && pullRole.equals(itemRole)) {
                        masterAddressList.add(serviceInstance.getIp() + ":" + serviceInstance.getPort());
                    }
                    if (itemRole.equals(BrokerRegistryEnum.SLAVE.getDesc()) && pullRole.equals(itemRole)) {
                        slaveAddressList.add(serviceInstance.getIp() + ":" + serviceInstance.getPort());
                    }
                    if (itemRole.equals(BrokerRegistryEnum.SINGLE.getDesc())) {
                        addressList.add(serviceInstance.getIp() + ":" + serviceInstance.getPort());
                    }
                }
            }
        }
        pullBrokerIpRespDTO.setMsgId(event.getMsgId());
        pullBrokerIpRespDTO.setMasterList(masterAddressList);
        pullBrokerIpRespDTO.setSlaveList(slaveAddressList);
        //防止ip重复
        pullBrokerIpRespDTO.setAddressList(addressList.stream().distinct().collect(Collectors.toList()));
        event.getChannelHandlerContext().writeAndFlush(new TcpMsg(NameServerResponseCode.PULL_BROKER_ADDRESS_SUCCESS.getCode(), JSON.toJSONBytes(pullBrokerIpRespDTO)));
    }
}
