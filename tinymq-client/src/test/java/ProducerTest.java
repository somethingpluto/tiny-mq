import org.junit.Before;
import org.junit.Test;
import org.tiny.mq.common.dto.MessageDTO;
import org.tiny.mq.producer.DefaultProducer;
import org.tiny.mq.producer.SendResult;

public class ProducerTest {
    private DefaultProducer producer = new DefaultProducer();

    @Before
    public void setup() {
        producer.setNameserverIp("127.0.0.1");
        producer.setNameserverPort(9090);
        producer.setNameserverUser("tiny_mq");
        producer.setNameserverPassword("tiny_mq");
        producer.start();
    }

    @Test
    public void testSendMessage() {
        for (int i = 0; i < 1000000000; i++) {
            try {
                MessageDTO messageDTO = new MessageDTO();
                messageDTO.setBody("send sync message".getBytes());
                messageDTO.setTopic("user_topic");
                SendResult sendResult = producer.sendMessage(messageDTO);
                System.out.println("send message:" + sendResult.getSendStatus());
            } catch (Exception e) {
                System.out.println(e);
            }

        }

    }
}
