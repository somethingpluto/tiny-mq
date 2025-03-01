import org.junit.Before;
import org.junit.Test;
import org.tiny.mq.common.dto.MessageDTO;
import org.tiny.mq.producer.DefaultProducer;

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
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setBody("send sync message".getBytes());
        messageDTO.setTopic("user_topic");
        producer.sendMessage(messageDTO);
        messageDTO.setBody("send async message".getBytes());
        producer.sendAsyncMessage(messageDTO);
    }
}
