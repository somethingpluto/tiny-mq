package org.tiny.mq.nameserver.event.spi.listener;

import com.alibaba.fastjson.JSON;
import org.tiny.mq.common.coder.TcpMsg;
import org.tiny.mq.common.dto.PullBrokerIpRespDTO;
import org.tiny.mq.common.enums.NameServerResponseCode;
import org.tiny.mq.common.enums.RegistryTypeEnum;
import org.tiny.mq.common.event.Listener;
import org.tiny.mq.nameserver.common.CommonCache;
import org.tiny.mq.nameserver.enums.PullBrokerIpRoleEnum;
import org.tiny.mq.nameserver.event.model.PullBrokerIpEvent;
import org.tiny.mq.nameserver.store.ServiceInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PullBrokerIpListener implements Listener<PullBrokerIpEvent> {

    @Override
    public void onReceive(PullBrokerIpEvent event) throws Exception {
        String pullRole = event.getRole();
        PullBrokerIpRespDTO pullBrokerIpRespDTO = new PullBrokerIpRespDTO();
        List<String> addressList = new ArrayList<>();
        Map<String, ServiceInstance> serviceInstanceMap = CommonCache.getServiceInstanceManager().getServiceInstanceMap();
        for (String reqId : serviceInstanceMap.keySet()) {
            ServiceInstance serviceInstance = serviceInstanceMap.get(reqId);
            if (RegistryTypeEnum.BROKER.getCode().equals(serviceInstance.getRegistryType())) {
                Map<String, Object> brokerAttrs = serviceInstance.getAttrs();
                String role = (String) brokerAttrs.get("role");
                if (PullBrokerIpRoleEnum.MASTER.getCode().equals(pullRole)
                        && PullBrokerIpRoleEnum.MASTER.getCode().equals(role)) {
                    addressList.add(serviceInstance.getIp() + ":" + serviceInstance.getPort());
                } else if (PullBrokerIpRoleEnum.SLAVE.getCode().equals(pullRole)
                        && PullBrokerIpRoleEnum.SLAVE.getCode().equals(role)) {
                    addressList.add(serviceInstance.getIp() + ":" + serviceInstance.getPort());
                } else if (PullBrokerIpRoleEnum.SINGLE.getCode().equals(pullRole)
                        && PullBrokerIpRoleEnum.SINGLE.getCode().equals(role)) {
                    addressList.add(serviceInstance.getIp() + ":" + serviceInstance.getPort());
                }
            }
        }
        pullBrokerIpRespDTO.setMsgId(event.getMsgId());
        pullBrokerIpRespDTO.setAddressList(addressList);
        event.getChannelHandlerContext().writeAndFlush(new TcpMsg(NameServerResponseCode.PULL_BROKER_ADDRESS_SUCCESS.getCode(),
                JSON.toJSONBytes(pullBrokerIpRespDTO)));
    }
}
