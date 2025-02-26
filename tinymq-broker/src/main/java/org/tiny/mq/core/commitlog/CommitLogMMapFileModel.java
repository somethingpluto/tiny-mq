package org.tiny.mq.core.commitlog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.common.constants.BrokerConstants;
import org.tiny.mq.common.dto.MessageDTO;
import org.tiny.mq.config.GlobalCache;
import org.tiny.mq.core.consumequeue.ConsumeQueueMemoryMapFileModel;
import org.tiny.mq.model.ModelManager;
import org.tiny.mq.model.commitlog.CommitLogFilePath;
import org.tiny.mq.model.commitlog.CommitLogModel;
import org.tiny.mq.model.commitlog.TopicModel;
import org.tiny.mq.model.consumequeue.ConsumeQueueItemModel;
import org.tiny.mq.model.consumequeue.QueueModel;
import org.tiny.mq.model.message.MessageModel;
import org.tiny.mq.utils.FileNameUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class CommitLogMMapFileModel {

    private final Logger logger = LoggerFactory.getLogger(CommitLogMMapFileModel.class);
    // 公平锁
    private final ReentrantLock appendMessageLock = new ReentrantLock(true);
    private String topicName;
    private MappedByteBuffer mappedByteBuffer;


    /**
     * 对topic下最新的文件进行内存映射
     *
     * @param topicName   主题名称
     * @param startOffset 开始位置偏移量
     * @param endOffset   结束位置偏移量
     */
    public void loadFileInMemory(String topicName, int startOffset, int endOffset) throws IOException {
        String filePath = this.getLatestCommitLogFile(topicName);
        this.topicName = topicName;
        int mapSize = endOffset - startOffset;
        this.doMemoryMap(filePath, startOffset, mapSize);
    }


    /**
     * 对指定文件进内存映射
     *
     * @param filePath    文件路径
     * @param startOffset 开始位置偏移量
     * @param mapSize     读取内容大小
     */
    private void doMemoryMap(String filePath, int startOffset, int mapSize) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("filepath is " + filePath + " invalid");
        }
        FileChannel fileChannel = new RandomAccessFile(filePath, "rw").getChannel();
        this.mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, startOffset, mapSize);
    }


    /**
     * 读取指定开始位置 指定大小的字节数据
     *
     * @param readOffset 开始位置偏移量
     * @param size       读取内容大小
     * @return 读取的字节数组
     */
    public byte[] readContent(int readOffset, int size) {
        byte[] content = new byte[size];
        for (int i = 0; i < size; i++) {
            byte b = mappedByteBuffer.get(readOffset + i);
            content[i] = b;
        }
        return content;
    }


    /**
     * 向文件中写入数据
     *
     * @param messageModel 消息对象
     * @param force        是否强制刷盘
     */
    public void writeContent(MessageModel messageModel, boolean force) {
        TopicModel topicModel = GlobalCache.getMQTopicModelMap().get(topicName);
        if (topicModel == null) {
            throw new IllegalArgumentException("topic:" + topicName + "model is null");
        }
        CommitLogModel commitLogModel = topicModel.getCommitLogModel();
        if (commitLogModel == null) {
            throw new IllegalArgumentException("commitLogModel is null");
        }
        try {
            appendMessageLock.lock();
            // 1.查看commit log文件是否还有空余空间
            this.checkCommitLogHasEnablePlace(messageModel);
            // 2.获取消息的字节数组表述
            byte[] bytesContent = messageModel.convertToBytes();
            // 3.消息写入commit log
            mappedByteBuffer.put(bytesContent);
            // 4.消息派发到队列
            this.dispatch(topicModel, bytesContent);
            // 5.消息指针后移
            commitLogModel.getOffset().addAndGet(bytesContent.length);
            if (force) {
                mappedByteBuffer.force();
            }
//            logger.info("{} append message:{}", topicName, messageModel);

        } catch (Exception e) {
            logger.error("write message error {}", e.toString());
        } finally {
            appendMessageLock.unlock();
        }
    }


    public void writeContent(MessageDTO messageDTO, boolean force) {
        TopicModel topicModel = GlobalCache.getMQTopicModelMap().get(messageDTO.getTopic());
        if (topicModel == null) {
            throw new IllegalArgumentException("topic:" + topicName + "model is null");
        }
        CommitLogModel commitLogModel = topicModel.getCommitLogModel();
        if (commitLogModel == null) {
            throw new IllegalArgumentException("commitLogModel is null");
        }
        MessageModel messageModel = new MessageModel();
        messageModel.setContent(messageDTO.getBody());
        try {
            appendMessageLock.lock();
            // 1.查看commit log文件是否还有空余空间
            this.checkCommitLogHasEnablePlace(messageModel);
            // 2.获取消息的字节数组表述
            byte[] bytesContent = messageModel.convertToBytes();
            // 3.消息写入commit log
            mappedByteBuffer.put(bytesContent);
            // 4.消息派发到队列
            this.dispatch(topicModel, bytesContent);
            // 5.消息指针后移
            commitLogModel.getOffset().addAndGet(bytesContent.length);
            if (force) {
                mappedByteBuffer.force();
            }
//            logger.info("{} append message:{}", topicName, messageModel);

        } catch (Exception e) {
            logger.error("write message error {}", e.toString());
        } finally {
            appendMessageLock.unlock();
        }
    }


    /**
     * 新增消息 同步到队列(做索引)
     *
     * @param topicModel   主题模型
     * @param bytesContent 字节内容
     */
    private void dispatch(TopicModel topicModel, byte[] bytesContent) {
        String fileName = topicModel.getCommitLogModel().getFileName();
        int offset = topicModel.getCommitLogModel().getOffset().get();
        // TODO: 获取待写入队列的id
        int queueId = 0;
        // 包装本次写入数据在文件中的索引信息
        ConsumeQueueItemModel consumeQueueItemModel = new ConsumeQueueItemModel();
        consumeQueueItemModel.setMsgLength(bytesContent.length);
        consumeQueueItemModel.setCommitLogFilename(Integer.parseInt(fileName));
        consumeQueueItemModel.setMsgIndex(offset);
        logger.info("consume queue item {}", consumeQueueItemModel);
        byte[] content = consumeQueueItemModel.convertToBytes();
        // 获取对应的队列索引文件，并将内容写入到对应的索引文件
        List<ConsumeQueueMemoryMapFileModel> consumeQueueMemoryMapFileModels = GlobalCache.getConsumeQueueMMapFileModelManager().get(topicName);
        ConsumeQueueMemoryMapFileModel consumeQueueMMpFileModel = consumeQueueMemoryMapFileModels.stream().filter(consumeQueueMemoryMapFileModel -> consumeQueueMemoryMapFileModel.getQueueId().equals(queueId)).findFirst().orElse(null);
        consumeQueueMMpFileModel.writeContent(content);
        // 写入时更新 刷新offset
        QueueModel queueModel = topicModel.getQueueList().get(queueId);
        queueModel.getLatestOffset().addAndGet(content.length);

    }

    public void writeContent(MessageModel messageModel) {
        this.writeContent(messageModel, false);
    }


    /**
     * 检测commit log 是否还存在剩余空间
     *
     * @param messageModel 消息体内容
     * @throws IOException 创建文件错误
     */
    private void checkCommitLogHasEnablePlace(MessageModel messageModel) throws IOException {
        // 根据topic name 获取 对应的model
        TopicModel topicModel = GlobalCache.getMQTopicModelMap().get(topicName);
        // 获取文件model
        CommitLogModel logModel = topicModel.getCommitLogModel();
        // 查看文件model剩余空间
        Long writeAbleOffsetNum = logModel.countDiff();
        // condition1: 文件剩余大小 不够message model 存放 进入创建新文件逻辑
        if (!(writeAbleOffsetNum >= messageModel.getContent().length)) {
            CommitLogFilePath commitLogFile = this.createCommitLogFile(topicName, logModel);
            logModel.setOffsetLimit(Long.valueOf(BrokerConstants.COMMIT_LOG_DEFAULT_MMAP_SIZE));
            logModel.setOffset(new AtomicInteger(0));
            logModel.setFileName(commitLogFile.getFileName());
            this.doMemoryMap(commitLogFile.getFilePath(), 0, BrokerConstants.COMMIT_LOG_DEFAULT_MMAP_SIZE);
        }
    }

    public void clean() {
        if (mappedByteBuffer == null || !mappedByteBuffer.isDirect() || mappedByteBuffer.capacity() == 0)
            return;
        invoke(invoke(viewed(mappedByteBuffer), "cleaner"), "clean");
    }

    private Object invoke(final Object target, final String methodName, final Class<?>... args) {
        return AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                try {
                    Method method = method(target, methodName, args);
                    method.setAccessible(true);
                    return method.invoke(target);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }
        });
    }


    private ByteBuffer viewed(ByteBuffer buffer) {
        String methodName = "viewedBuffer";
        Method[] methods = buffer.getClass().getMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals("attachment")) {
                methodName = "attachment";
                break;
            }
        }
        ByteBuffer viewedBuffer = (ByteBuffer) invoke(buffer, methodName);
        if (viewedBuffer == null)
            return buffer;
        else
            return viewed(viewedBuffer);
    }

    private Method method(Object target, String methodName, Class<?>[] args)
            throws NoSuchMethodException {
        try {
            return target.getClass().getMethod(methodName, args);
        } catch (NoSuchMethodException e) {
            return target.getClass().getDeclaredMethod(methodName, args);
        }
    }

    private String getLatestCommitLogFile(String topicName) {
        TopicModel topicModel = ModelManager.getTopicModel(topicName);
        CommitLogModel commitLogModel = topicModel.getCommitLogModel();
        Long diff = commitLogModel.countDiff();
        String filePath = null;
        if (diff == 0) {
            // 已经写满
            CommitLogFilePath commitLogFile = this.createCommitLogFile(topicName, commitLogModel);
            filePath = commitLogFile.getFilePath();
        } else if (diff > 0) {
            // 还有空间 获取文件路径
            filePath = FileNameUtil.buildCommitLogFilePath(topicName, commitLogModel.getFileName());
        }
        return filePath;
    }

    private CommitLogFilePath createCommitLogFile(String topicName, CommitLogModel commitLogModel) {
        String newFileName = FileNameUtil.incrCommitLogFileName(commitLogModel.getFileName());
        String newFilePath = FileNameUtil.buildCommitLogFilePath(topicName, newFileName);
        File newCommitLogFile = new File(newFilePath);
        try {
            newCommitLogFile.createNewFile();
            logger.info("create new commit log topic:{} fileName:{}", topicName, newFileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new CommitLogFilePath(newFileName, newFilePath);
    }

}
