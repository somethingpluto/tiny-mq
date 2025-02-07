package org.tiny.mq.common.codec;

import org.tiny.mq.common.constants.NameServerConstants;

import java.util.Arrays;

public class TcpMessage {

    // 魔数
    private short magic;
    // 消息类型
    private int code;
    // 数据内容长度
    private int len;
    // 数据内容
    private byte[] body;

    public TcpMessage(int code, byte[] body) {
        this.magic = NameServerConstants.DEFAULT_MAGIC_NUM;
        this.code = code;
        this.body = body;
        this.len = body.length;
    }

    public short getMagic() {
        return magic;
    }

    public void setMagic(short magic) {
        this.magic = magic;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getLen() {
        return len;
    }


    public void setLen(int len) {
        this.len = len;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "TcpMessage{" +
                "magic=" + magic +
                ", code=" + code +
                ", len=" + len +
                ", body=" + Arrays.toString(body) +
                '}';
    }

}
