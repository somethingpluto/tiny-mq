package org.tiny.mq.model.commitlog;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * Commit Log文件模型
 */
public class CommitLogModel {
    /**
     * CommitLog文件名称
     */
    private String fileName;
    /**
     * commitLog文件写入的最大体积
     */
    private Long offsetLimit;
    /**
     * 最新写入log文件的地址
     */
    private AtomicInteger offset;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getOffsetLimit() {
        return offsetLimit;
    }

    public void setOffsetLimit(Long offsetLimit) {
        this.offsetLimit = offsetLimit;
    }

    public AtomicInteger getOffset() {
        return offset;
    }

    public void setOffset(AtomicInteger offset) {
        this.offset = offset;
    }

    public Long countDiff() {
        return this.offsetLimit - this.offset.get();
    }

    @Override
    public String toString() {
        return "CommitLogModel{" +
                "fileName='" + fileName + '\'' +
                ", offsetLimit=" + offsetLimit +
                ", offset=" + offset +
                '}';
    }
}
