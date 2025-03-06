package org.tiny.mq.model;

import com.alibaba.fastjson.JSON;
import org.tiny.mq.utils.ByteConvertUtils;


public class ConsumeQueueDetailModel {

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
        byte[] commitLogFileNameBytes = ByteConvertUtils.intToBytes(commitLogFilename);
        byte[] msgIndexBytes = ByteConvertUtils.intToBytes(msgIndex);
        byte[] msgLengthBytes = ByteConvertUtils.intToBytes(msgLength);
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
        this.setCommitLogFilename(ByteConvertUtils.bytesToInt(ByteConvertUtils.readInPos(body, 0, 4)));
        this.setMsgIndex(ByteConvertUtils.bytesToInt(ByteConvertUtils.readInPos(body, 4, 4)));
        this.setMsgLength(ByteConvertUtils.bytesToInt(ByteConvertUtils.readInPos(body, 8, 4)));
    }

    public static void main(String[] args) {
        ConsumeQueueDetailModel consumeQueueDetailModel = new ConsumeQueueDetailModel();
        consumeQueueDetailModel.setCommitLogFilename(1);
        consumeQueueDetailModel.setMsgIndex(1522);
        consumeQueueDetailModel.setMsgLength(26);
        byte[] body = consumeQueueDetailModel.convertToBytes();
        consumeQueueDetailModel.buildFromBytes(body);
        System.out.println(JSON.toJSONString(consumeQueueDetailModel));
    }
}
