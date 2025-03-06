import org.junit.Before;
import org.junit.Test;
import org.tiny.mq.common.dto.MessageDTO;
import org.tiny.mq.producer.DefaultProducerImpl;
import org.tiny.mq.producer.SendResult;

public class TestProducerSuite {

    private DefaultProducerImpl producer;

    @Before
    public void setUp() {
        producer = new DefaultProducerImpl();
        producer.setNsIp("127.0.0.1");
        producer.setNsPort(9093);
        producer.setNsPwd("tiny_mq");
        producer.setNsUser("tiny_mq");
        producer.start();
    }

    @Test
    public void sendMsg() throws InterruptedException {
        long i = 1;
        while (true) {
            MessageDTO messageDTO = new MessageDTO();
            messageDTO.setTopic("order_cancel_topic");
            messageDTO.setBody(("mq content-" + i).getBytes());
            SendResult sendResult = producer.send(messageDTO);
            System.out.println(sendResult.getSendStatus());
            i++;
        }
    }


}
