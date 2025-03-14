import org.junit.Test;
import org.tiny.mq.consumer.ConsumeMessage;
import org.tiny.mq.consumer.ConsumeResult;
import org.tiny.mq.consumer.DefaultMqConsumer;
import org.tiny.mq.consumer.MessageConsumeListener;

import java.util.List;


public class TestConsumerSuite {


    private DefaultMqConsumer consumer;

    @Test
    public void testConsumeMsg() throws InterruptedException {
        consumer = new DefaultMqConsumer();
        consumer.setNsIp("127.0.0.1");
        consumer.setNsPort(8990);
        consumer.setNsPwd("eagle_mq");
        consumer.setNsUser("eagle_mq");
        consumer.setTopic("order_cancel_topic");
        consumer.setConsumeGroup("test-consume-group");
        consumer.setBatchSize(1);
        consumer.setMessageConsumeListener(new MessageConsumeListener() {
            @Override
            public ConsumeResult consume(List<ConsumeMessage> consumeMessages) {
                for (ConsumeMessage consumeMessage : consumeMessages) {
                    System.out.println("消费端获取的数据内容:" + new String(consumeMessage.getBody()));
                }
                return ConsumeResult.CONSUME_SUCCESS();
            }
        });
        consumer.start();
    }

    @Test
    public void testConsumeMsg2() throws InterruptedException {
        consumer = new DefaultMqConsumer();
        consumer.setNsIp("127.0.0.1");
        consumer.setNsPort(9093);
        consumer.setNsPwd("tiny_mq");
        consumer.setNsUser("tiny_mq");
        consumer.setTopic("order_cancel_topic");
        consumer.setConsumeGroup("test-consume-group");
        consumer.setBatchSize(1);
        consumer.setMessageConsumeListener(new MessageConsumeListener() {
            @Override
            public ConsumeResult consume(List<ConsumeMessage> consumeMessages) {
                for (ConsumeMessage consumeMessage : consumeMessages) {
                    System.out.println("消费端获取的数据内容:" + new String(consumeMessage.getBody()));
                }
                return ConsumeResult.CONSUME_SUCCESS();
            }
        });
        consumer.start();
    }

}
