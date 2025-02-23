package org.tiny.mq.nameserver.replication;

import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.common.codec.TcpMessage;
import org.tiny.mq.common.enums.NameServerEventCode;
import org.tiny.mq.nameserver.config.GlobalConfig;
import org.tiny.mq.nameserver.eventbus.event.SlaveHeartBeatEvent;
import org.tiny.mq.nameserver.eventbus.event.StartReplicationEvent;

import java.util.concurrent.TimeUnit;

public class SlaveReplicationHeartBeatTask extends ReplicationTask {

    private static final Logger logger = LoggerFactory.getLogger(SlaveReplicationHeartBeatTask.class);

    public SlaveReplicationHeartBeatTask(String taskName) {
        super(taskName);
    }

    @Override
    void startTask() {
        StartReplicationEvent startReplicationEvent = new StartReplicationEvent();
        startReplicationEvent.setUser(GlobalConfig.getNameserverConfig().getNameserverUser());
        startReplicationEvent.setPassword(GlobalConfig.getNameserverConfig().getNameserverPassword());
        TcpMessage startReplicationMsg = new TcpMessage(NameServerEventCode.START_REPLICATION.getCode(), JSON.toJSONBytes(startReplicationEvent));
        GlobalConfig.getConnectNodeChannel().writeAndFlush(startReplicationMsg);
        while (true) {
            try {
                TimeUnit.SECONDS.sleep(3);
                Channel channel = GlobalConfig.getConnectNodeChannel();
                TcpMessage tcpMessage = new TcpMessage(NameServerEventCode.SLAVE_HEART_BEAT.getCode(), JSON.toJSONBytes(new SlaveHeartBeatEvent()));
                channel.writeAndFlush(tcpMessage);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }
}
