import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.tiny.mq.common.dto.MessageDTO;
import org.tiny.mq.producer.DefaultProducerImpl;
import org.tiny.mq.producer.SendResult;

import java.util.concurrent.TimeUnit;

public class TestProducerSuite {

    private DefaultProducerImpl producer;

    @Before
    public void setUp() {
        producer = new DefaultProducerImpl();
        producer.setNsIp("127.0.0.1");
        producer.setNsPort(9093);
        producer.setNsPwd("tiny_mq");
        producer.setNsUser("tiny_mq");
        producer.setBrokerClusterGroup("tiny_mq_test_group");
        producer.start();
    }

    @Test
    public void sendSyncMsg() throws InterruptedException {
        for (int i = 0; i < 1000000; i++) {
            try {
                MessageDTO messageDTO = new MessageDTO();
                messageDTO.setTopic("user_all_info");
                messageDTO.setBody(("mq content-" + i).getBytes());
                SendResult sendResult = producer.send(messageDTO);
                System.out.println(JSON.toJSONString(sendResult));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void sendAsyncMsg() throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            TimeUnit.SECONDS.sleep(1);
            MessageDTO messageDTO = new MessageDTO();
            messageDTO.setTopic("order_enter");
            messageDTO.setBody(("mq content-" + i).getBytes());
            producer.sendAsync(messageDTO);
            System.out.println("async send ok");
        }
    }

    @Test
    public void sendUserEnterMsg() {
        for (int i = 0; i < 100; i++) {
            try {
                MessageDTO messageDTO = new MessageDTO();
                messageDTO.setTopic("user_enter");
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("userId", i);
                jsonObject.put("level", 1);
                jsonObject.put("enerTime", System.currentTimeMillis());
                messageDTO.setBody(jsonObject.toJSONString().getBytes());
                SendResult sendResult = producer.send(messageDTO);
                System.out.println(JSON.toJSONString(sendResult));
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 设计延迟消息
     * 时间范围 过大的延迟时间 会导致数据一直没有执行消耗时间轮
     */
    @Test
    public void sendUserEnterDelayMsg() {
        for (int i = 0; i < 100; i++) {
            try {
                MessageDTO messageDTO = new MessageDTO();
                messageDTO.setTopic("user_enter");
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("userId", i);
                jsonObject.put("level", 1);
                jsonObject.put("enerTime", System.currentTimeMillis());
                messageDTO.setBody(jsonObject.toJSONString().getBytes());
                messageDTO.setDelay(4);
                SendResult sendResult = producer.send(messageDTO);
                System.out.println(JSON.toJSONString(sendResult));
                TimeUnit.SECONDS.sleep(4);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Test
    public void testClusterBroker() {
        System.out.println("启动broker集群部署模式");
    }


}
