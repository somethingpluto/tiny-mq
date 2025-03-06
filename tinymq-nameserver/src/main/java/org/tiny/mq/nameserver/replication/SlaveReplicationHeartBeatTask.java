package org.tiny.mq.nameserver.replication;

import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.common.coder.TcpMsg;
import org.tiny.mq.common.enums.NameServerEventCode;
import org.tiny.mq.nameserver.common.CommonCache;
import org.tiny.mq.nameserver.event.model.SlaveHeartBeatEvent;
import org.tiny.mq.nameserver.event.model.StartReplicationEvent;

import java.util.concurrent.TimeUnit;

public class SlaveReplicationHeartBeatTask extends ReplicationTask {

    private final Logger logger = LoggerFactory.getLogger(SlaveReplicationHeartBeatTask.class);

    public SlaveReplicationHeartBeatTask(String taskName) {
        super(taskName);
    }

    @Override
    public void startTask() {
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        StartReplicationEvent startReplicationEvent = new StartReplicationEvent();
        startReplicationEvent.setUser(CommonCache.getNameserverProperties().getNameserverUser());
        startReplicationEvent.setPassword(CommonCache.getNameserverProperties().getNameserverPwd());
        TcpMsg startReplicationMsg = new TcpMsg(NameServerEventCode.START_REPLICATION.getCode(), JSON.toJSONBytes(startReplicationEvent));
        CommonCache.getConnectNodeChannel().writeAndFlush(startReplicationMsg);
        while (true) {
            try {
                TimeUnit.SECONDS.sleep(3);
                //发送数据给到主节点
                Channel channel = CommonCache.getConnectNodeChannel();
                TcpMsg tcpMsg = new TcpMsg(NameServerEventCode.SLAVE_HEART_BEAT.getCode(), JSON.toJSONBytes(new SlaveHeartBeatEvent()));
                channel.writeAndFlush(tcpMsg);
                logger.info("从节点发送心跳数据给master");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
