package org.tiny.mq;

import org.tiny.mq.producer.DefaultProducer;

public class Main {
    public static void main(String[] args) {
        DefaultProducer producer = new DefaultProducer();
        producer.setNameserverIp("127.0.0.1");
        producer.setNameserverPort(9090);
        producer.setNameserverUser("tiny_mq");
        producer.setNameserverPassword("tiny_mq");
        producer.start();
    }
}