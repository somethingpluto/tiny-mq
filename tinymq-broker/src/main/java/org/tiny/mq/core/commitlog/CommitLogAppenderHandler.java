package org.tiny.mq.core.commitlog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiny.mq.common.constants.BrokerConstants;
import org.tiny.mq.common.dto.MessageDTO;
import org.tiny.mq.config.GlobalCache;
import org.tiny.mq.model.commitlog.TopicModel;
import org.tiny.mq.utils.FileNameUtil;

import java.io.IOException;

public class CommitLogAppenderHandler {

    private final Logger logger = LoggerFactory.getLogger(CommitLogAppenderHandler.class);
    private final CommitLogMMapFileModelManager commitLogMMapFileModelManager = GlobalCache.getMemoryMapFileModelManager();


    public void prepareMMapLoading(TopicModel topicModel) throws IOException {
        CommitLogMMapFileModel commitLogMMapFileModel = new CommitLogMMapFileModel();
        String topicName = topicModel.getTopicName();
        String filePath = FileNameUtil.buildCommitLogFilePath(topicName, FileNameUtil.buildFirstCommitLogName());
        logger.info("prepare commit log filePath {}", filePath);
        commitLogMMapFileModel.loadFileInMemory(topicName, 0, BrokerConstants.COMMIT_LOG_DEFAULT_MMAP_SIZE);
        commitLogMMapFileModelManager.put(topicName, commitLogMMapFileModel);
    }

    /**
     * 指定主题追加信息
     *
     * @param topic   主题
     * @param content 信息内容
     */
//    public void appendMessage(String topic, byte[] content) {
//        CommitLogMMapFileModel commitLogMMapFileModel = commitLogMMapFileModelManager.get(topic);
//        if (commitLogMMapFileModel == null) {
//            throw new RuntimeException("topic invalid");
//        }
//
//        MessageModel messageModel = new MessageModel();
//        messageModel.setContent(content);
//        commitLogMMapFileModel.writeContent(messageModel);
//    }
    public void appendMessage(MessageDTO messageDTO) {
        CommitLogMMapFileModel commitLogMMapFileModel = GlobalCache.getCommitLogMMapFileModelManager().get(messageDTO.getTopic());
        if (commitLogMMapFileModel == null) {
            throw new RuntimeException("topic is valid");
        }
        commitLogMMapFileModel.writeContent(messageDTO, true);
    }

}
