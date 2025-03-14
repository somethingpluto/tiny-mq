package org.tiny.mq.netty.nameserver;

import com.alibaba.fastjson2.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.cache.CommonCache;
import org.tiny.mq.common.coder.TcpMsg;
import org.tiny.mq.common.dto.HeartBeatDTO;
import org.tiny.mq.common.enums.NameServerEventCode;
import org.tiny.mq.common.enums.NameServerResponseCode;
import org.tiny.mq.common.remote.NameServerNettyRemoteClient;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class HeartBeatTaskManager {

    private AtomicInteger flag = new AtomicInteger(0);
    private static Logger logger = LoggerFactory.getLogger(HeartBeatTaskManager.class);

    //开启心跳传输任务
    public void startTask() {
        if (flag.getAndIncrement() >= 1) {
            return;
        }
        Thread heartBeatRequestTask = new Thread(new HeartBeatRequestTask());
        heartBeatRequestTask.setName("heart-beat-request-task");
        heartBeatRequestTask.start();
    }


    private class HeartBeatRequestTask implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(3);
                    //心跳包不需要额外透传过多的参数，只需要告诉nameserver这个channel依然存活即可
                    NameServerNettyRemoteClient nameServerNettyRemoteClient = CommonCache.getNameServerClient().getNameServerNettyRemoteClient();
                    HeartBeatDTO heartBeatDTO = new HeartBeatDTO();
                    heartBeatDTO.setMsgId(UUID.randomUUID().toString());
                    TcpMsg tcpMsg = new TcpMsg(NameServerEventCode.HEART_BEAT.getCode(), JSON.toJSONBytes(heartBeatDTO));
                    TcpMsg hearBeatResp = nameServerNettyRemoteClient.sendSyncMsg(tcpMsg, heartBeatDTO.getMsgId());
                    if (NameServerResponseCode.HEART_BEAT_SUCCESS.getCode() != hearBeatResp.getCode()) {
                        logger.info("heart beat from nameserver is error");
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
