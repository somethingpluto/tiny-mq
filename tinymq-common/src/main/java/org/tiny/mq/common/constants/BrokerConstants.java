package org.tiny.mq.common.constants;

public class BrokerConstants {
    public static final String TINY_MQ_HOME_PATH = "tiny_mq_home_path";
    public static final String BASE_COMMIT_PATH = "/broker/commitlog/";
    public static final String BASE_CONSUME_QUEUE_PATH = "/broker/consumequeue";
    public static final String SPLIT = "/";
    public static final Integer COMMIT_LOG_DEFAULT_MMAP_SIZE = 1024 * 1024;

    public static final Long DEFAULT_REFRESH_MQ_TOPIC_TIME_SETP = 1L;
    public static final Long DEFAULT_REFRESH_CONSUME_QUEUE_OFFSET_TIME_STEP = 1L;
    public static final int CONSUME_QUEUE_EACH_MSG_SIZE = 12;

    public static final String INIT_COMMITLOG_FILE_NAME = "00000000";
    public static final String BROKER_PROPERTIES_PATH = "/broker/config/broker.properties";
    public static final String CONFIG_NAMESERVER_IP = "nameserver.ip";
    public static final String CONFIG_NAMESERVER_PORT = "nameserver.port";
    public static final String CONFIG_NAMESERVER_USER = "nameserver.user";
    public static final String CONFIG_NAMESERVER_PASSWORD = "nameserver.password";
    public static final String CONFIG_BROKER_PORT = "broker.port";

}
