package org.tiny.mq.model.consumequeue;

import java.util.concurrent.atomic.AtomicInteger;

public class QueueModel {

    private Integer id;
    private String fileName;
    private Integer offsetLimit;
    private AtomicInteger latestOffset;
    private Integer lastOffset;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getOffsetLimit() {
        return offsetLimit;
    }

    public void setOffsetLimit(Integer offsetLimit) {
        this.offsetLimit = offsetLimit;
    }

    public AtomicInteger getLatestOffset() {
        return latestOffset;
    }

    public void setLatestOffset(AtomicInteger latestOffset) {
        this.latestOffset = latestOffset;
    }

    public Integer getLastOffset() {
        return lastOffset;
    }

    public void setLastOffset(Integer lastOffset) {
        this.lastOffset = lastOffset;
    }

    public int countDiff() {
        return this.getOffsetLimit() - this.getLatestOffset().get();
    }

    @Override
    public String toString() {
        return "QueueModel{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", offsetLimit=" + offsetLimit +
                ", latestOffset=" + latestOffset +
                ", lastOffset=" + lastOffset +
                '}';
    }
}
