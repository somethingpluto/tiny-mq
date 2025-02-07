package org.tiny.mq.core.consumequeue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.common.constants.BrokerConstants;
import org.tiny.mq.config.GlobalCache;
import org.tiny.mq.model.commitlog.TopicModel;
import org.tiny.mq.model.consumequeue.QueueModel;
import org.tiny.mq.utils.FileNameUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class ConsumeQueueMemoryMapFileModel {
    private File file;
    private MappedByteBuffer mappedByteBuffer;
    private ByteBuffer readBuffer;
    private FileChannel fileChannel;
    private String topic;
    private Integer queueId;
    private String consumeQueueFileName;
    private final ReentrantLock putMessageLock = new ReentrantLock(true);
    private final Logger logger = LoggerFactory.getLogger(ConsumeQueueMemoryMapFileModel.class);

    public void loadFileInMemory(String topicName, Integer queueId, int startOffset, int latestWriteOffset, int mappedSize) throws IOException {
        this.topic = topicName;
        this.queueId = queueId;
        String filePath = this.getLatestConsumeQueueFile();
        this.doMemoryMap(filePath, startOffset, latestWriteOffset, mappedSize);


    }

    public void writeContent(byte[] content, boolean force) {
        try {
            putMessageLock.lock();
            mappedByteBuffer.put(content);
            if (force) {
                mappedByteBuffer.force();
            }
        } finally {
            putMessageLock.unlock();
        }
    }

    public void writeContent(byte[] content) {
        writeContent(content, false);
    }

    public byte[] readContent(int pos) {
        ByteBuffer readBuf = readBuffer.slice();
        readBuf.position(pos);
        byte[] content = new byte[BrokerConstants.CONSUME_QUEUE_EACH_MSG_SIZE];
        readBuf.get(content);
        return content;
    }


    private String getLatestConsumeQueueFile() {
        TopicModel topicModel = GlobalCache.getMQTopicModelMap().get(this.topic);
        if (topicModel == null) {
            throw new IllegalArgumentException("topic is invalid topicName is " + topic);
        }
        List<QueueModel> queueList = topicModel.getQueueList();
        QueueModel queueModel = queueList.get(queueId);
        if (queueModel == null) {
            throw new IllegalArgumentException("queueId is invalid queueId is " + queueId);
        }
        Integer diff = queueModel.getOffsetLimit();
        String filePath = null;
        if (diff == 0) {
            this.createNewConsumeQueueFile(queueModel.getFileName());
        } else if (diff > 0) {
            filePath = FileNameUtil.buildConsumeQueueFilePath(topic, queueId, queueModel.getFileName());
        }
        return filePath;
    }

    private void doMemoryMap(String filePath, int startOffset, int latestWriteOffset, int mappedSize) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("filepath is " + filePath + " invalid");
        }
        this.fileChannel = new RandomAccessFile(file, "rw").getChannel();
        this.mappedByteBuffer = this.fileChannel.map(FileChannel.MapMode.READ_WRITE, startOffset, mappedSize);
        this.readBuffer = mappedByteBuffer.slice();
        this.mappedByteBuffer.position(latestWriteOffset);

    }

    private String createNewConsumeQueueFile(String fileName) {
        String newFileName = FileNameUtil.incrConsumeQueueFileName(fileName);
        String newFIlePath = FileNameUtil.buildConsumeQueueFilePath(topic, queueId, newFileName);
        File newFile = new File(newFIlePath);
        try {
            newFile.createNewFile();
            logger.info("create consume queue file file name:{} file path:{}", newFileName, newFIlePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return newFIlePath;
    }

    public Integer getQueueId() {
        return queueId;
    }
}
