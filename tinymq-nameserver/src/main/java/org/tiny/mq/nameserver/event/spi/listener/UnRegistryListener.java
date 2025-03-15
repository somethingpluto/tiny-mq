package org.tiny.mq.nameserver.event.spi.listener;

import com.alibaba.fastjson2.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.common.coder.TcpMsg;
import org.tiny.mq.common.enums.BrokerRegistryEnum;
import org.tiny.mq.common.enums.NameServerResponseCode;
import org.tiny.mq.common.event.Listener;
import org.tiny.mq.nameserver.common.CommonCache;
import org.tiny.mq.nameserver.event.model.UnRegistryEvent;
import org.tiny.mq.nameserver.store.ServiceInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class UnRegistryListener implements Listener<UnRegistryEvent> {

    private static final Logger logger = LoggerFactory.getLogger(UnRegistryListener.class);

    @Override
    public void onReceive(UnRegistryEvent event) throws IllegalAccessException {
        ChannelHandlerContext channelHandlerContext = event.getChannelHandlerContext();
        String reqId = (String) channelHandlerContext.attr(AttributeKey.valueOf("reqId")).get();
        if (StringUtil.isNullOrEmpty(reqId)) {
            channelHandlerContext.writeAndFlush(new TcpMsg(NameServerResponseCode.ERROR_USER_OR_PASSWORD.getCode(), NameServerResponseCode.ERROR_USER_OR_PASSWORD.getDesc().getBytes()));
            channelHandlerContext.close();
            throw new IllegalAccessException("error account to connected!");
        }
        // 在下线监听器里面识别master节点断开的场景
        ServiceInstance serviceInstance = CommonCache.getServiceInstanceManager().get(reqId);
        Map<String, Object> attrs = serviceInstance.getAttrs();
        String role = (String) attrs.getOrDefault("role", "");
        if (role.equals(BrokerRegistryEnum.SINGLE.getDesc())) {
            singleModeUnRegistry();
        } else if (role.equals(BrokerRegistryEnum.MASTER.getDesc())) {
            masterSlaveModeMasterUnRegistry(reqId, serviceInstance);
        } else if (role.equals(BrokerRegistryEnum.SLAVE.getDesc())) {
            // 从节点删除
            masterSlaveModeSlaveUnRegistry(reqId, serviceInstance);
        }
    }

    private void masterSlaveModeSlaveUnRegistry(String reqId, ServiceInstance serviceInstance) {
        CommonCache.getServiceInstanceManager().remove(reqId);
    }

    private void singleModeUnRegistry() {

    }

    private void masterSlaveModeMasterUnRegistry(String reqId, ServiceInstance serviceInstance) {
        String group = (String) serviceInstance.getAttrs().getOrDefault("group", "");
        logger.info("master node un registry");
        List<ServiceInstance> slaveList = new ArrayList<>();
        for (ServiceInstance item : CommonCache.getServiceInstanceManager().getServiceInstanceMap().values()) {
            String itemGroup = (String) item.getAttrs().getOrDefault("group", "");
            String itemRole = (String) item.getAttrs().getOrDefault("role", "");
            if (group.equals(itemGroup) && itemRole.equals(BrokerRegistryEnum.SLAVE.getDesc())) { // 是这个主节点同一个组里面的broker 那么就是从节点
                slaveList.add(item);
            }
        }
        logger.info("slave info list {}", JSON.toJSONString(slaveList));
        // 选取出最新版本的slave节点
        long maxVersion = 0l;
        ServiceInstance newMaserBroker = null;
        for (ServiceInstance slaveNode : slaveList) {
            long latestVersion = (long) slaveNode.getAttrs().getOrDefault("latestVersion", 0);
            if (maxVersion < latestVersion) {
                newMaserBroker = slaveNode;
                maxVersion = latestVersion;
            }
        }
        // 删除master节点
        CommonCache.getServiceInstanceManager().remove(reqId);
        if (newMaserBroker != null) {
            newMaserBroker.getAttrs().put("role", "master");
        }
        // 重新设置主从关系
        CommonCache.getServiceInstanceManager().reload(slaveList);
        logger.info("new cluster node is:{}", JSON.toJSONString(slaveList));
    }

}
