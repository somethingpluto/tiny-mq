import org.junit.Before;
import org.junit.Test;
import org.tiny.mq.common.dto.ConsumeMessageDTO;
import org.tiny.mq.consumer.ConsumeResult;
import org.tiny.mq.consumer.DefaultConsumer;
import org.tiny.mq.consumer.MessageConsumeListener;

import java.util.List;


public class ConsumerTest {
    private DefaultConsumer consumer = new DefaultConsumer();

    @Before
    public void setup() {
        consumer.setNameserverIP("127.0.0.1");
        consumer.setNameserverPort(9090);
        consumer.setNameserverUser("tiny_mq");
        consumer.setNameserverPassword("tiny_mq");

    }

    @Test
    public void testStart() {
        consumer.setMessageConsumeListener(new MessageConsumeListener() {
            @Override
            public ConsumeResult consume(List<ConsumeMessageDTO> consumeMessageList) {
                for (ConsumeMessageDTO message : consumeMessageList) {
                    System.out.println("get message:" + message);
                }
                return ConsumeResult.CONSUME_SUCCESS();
            }
        });
        consumer.start();
    }
}
