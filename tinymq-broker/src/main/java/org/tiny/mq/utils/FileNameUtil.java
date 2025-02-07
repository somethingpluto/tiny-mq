package org.tiny.mq.utils;

import org.tiny.mq.common.constants.BrokerConstants;
import org.tiny.mq.config.GlobalCache;

import java.io.File;
import java.io.IOException;

public class FileNameUtil {
    public static String buildFirstCommitLogName() {
        return "00000000";
    }

    public static String buildCommitLogFilePath(String topicName, String commitLogFileName) {
        return GlobalCache.getConfig().getBasePath()
                + BrokerConstants.BASE_COMMIT_PATH
                + topicName
                + BrokerConstants.SPLIT
                + commitLogFileName;
    }

    /**
     * 根据老的commitLog文件名生成新的commitLog文件名
     *
     * @param oldFileName
     * @return
     */
    public static String incrCommitLogFileName(String oldFileName) {
        if (oldFileName.length() != 8) {
            throw new IllegalArgumentException("fileName must has 8 chars");
        }
        Long fileIndex = Long.valueOf(oldFileName);
        fileIndex++;
        String newFileName = String.valueOf(fileIndex);
        int newFileNameLen = newFileName.length();
        int needFullLen = 8 - newFileNameLen;
        if (needFullLen < 0) {
            throw new RuntimeException("unKnow fileName error");
        }
        StringBuffer stb = new StringBuffer();
        for (int i = 0; i < needFullLen; i++) {
            stb.append("0");
        }
        stb.append(newFileName);
        return stb.toString();
    }

    public static String buildConsumeQueueFilePath(String topicName, Integer queueId, String consumeQueueFileName) {
        return GlobalCache.getConfig().getBasePath()
                + BrokerConstants.BASE_CONSUME_QUEUE_PATH
                + BrokerConstants.SPLIT
                + topicName
                + BrokerConstants.SPLIT
                + queueId
                + BrokerConstants.SPLIT
                + consumeQueueFileName;
    }

    public static String incrConsumeQueueFileName(String oldFileName) {
        return incrCommitLogFileName(oldFileName);
    }


    public static boolean createFile(File file) throws IOException {
        if (file.exists()) {
            return true;
        } else {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw e;
            }
        }
        return file.exists();
    }
}
