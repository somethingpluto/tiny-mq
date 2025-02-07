package org.tiny.mq.netty.nameserver.task;

import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.common.codec.TcpMessage;
import org.tiny.mq.common.enums.NameServerEventCode;
import org.tiny.mq.config.GlobalCache;
import org.tiny.mq.netty.nameserver.NameServerClient;

import java.util.concurrent.TimeUnit;

public class HeartBeatTask implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(HeartBeatTask.class);

    @Override
    public void run() {
        while (true) {
            try {
                TimeUnit.SECONDS.sleep(3);
                // 心跳包不需要传输额外的数据
                NameServerClient client = GlobalCache.getNameServerClient();
                Channel channel = client.getChannel();
                TcpMessage message = new TcpMessage(NameServerEventCode.HEART_BEAT.getCode(), new byte[]{});
                channel.writeAndFlush(message);
                logger.info("broker heart beat");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
