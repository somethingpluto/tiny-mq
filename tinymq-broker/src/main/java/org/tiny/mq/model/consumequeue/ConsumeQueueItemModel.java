package org.tiny.mq.model.consumequeue;

import org.tiny.mq.utils.ByteConvertUtil;

public class ConsumeQueueItemModel {
    private int commitLogFilename;
    //4byte
    private int msgIndex; //commitlog数据存储的地址，mmap映射的地址，Integer.MAX校验

    private int msgLength;

    public int getCommitLogFilename() {
        return commitLogFilename;
    }

    public void setCommitLogFilename(int commitLogFilename) {
        this.commitLogFilename = commitLogFilename;
    }

    public void setMsgIndex(int msgIndex) {
        this.msgIndex = msgIndex;
    }

    public int getMsgIndex() {
        return msgIndex;
    }


    public int getMsgLength() {
        return msgLength;
    }

    public void setMsgLength(int msgLength) {
        this.msgLength = msgLength;
    }

    public byte[] convertToBytes() {
        byte[] commitLogFileNameBytes = ByteConvertUtil.intToBytes(commitLogFilename);
        byte[] msgIndexBytes = ByteConvertUtil.intToBytes(msgIndex);
        byte[] msgLengthBytes = ByteConvertUtil.intToBytes(msgLength);
        byte[] finalBytes = new byte[12];
        int p = 0;
        for (int i = 0; i < 4; i++) {
            finalBytes[p++] = commitLogFileNameBytes[i];
        }
        for (int i = 0; i < 4; i++) {
            finalBytes[p++] = msgIndexBytes[i];
        }
        for (int i = 0; i < 4; i++) {
            finalBytes[p++] = msgLengthBytes[i];
        }
        return finalBytes;
    }

    public void buildFromBytes(byte[] body) {
        this.setCommitLogFilename(ByteConvertUtil.byteToInt(ByteConvertUtil.readInPos(body,0,4)));
        this.setMsgIndex(ByteConvertUtil.byteToInt(ByteConvertUtil.readInPos(body,4,4)));
        this.setMsgLength(ByteConvertUtil.byteToInt(ByteConvertUtil.readInPos(body,8,4)));
    }

    @Override
    public String toString() {
        return "ConsumeQueueItemModel{" +
                "commitLogFilename=" + commitLogFilename +
                ", msgIndex=" + msgIndex +
                ", msgLength=" + msgLength +
                '}';
    }
}
