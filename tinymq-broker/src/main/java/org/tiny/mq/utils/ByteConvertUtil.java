package org.tiny.mq.utils;

public class ByteConvertUtil {
    /**
     * int 类型转换为byte类型
     * @param value
     * @return
     */
    public static byte[] intToBytes(int value){
        byte[] src = new byte[4];
        src[3] = (byte) ((value >> 24) & 0xFF);
        src[2] = (byte) ((value >> 16) & 0xFF);
        src[1] = (byte) ((value >> 8) & 0xFF);
        src[0] = (byte) (value & 0xFF);
        return src;
    }


    /**
     * byte 数组转换为int
     * @param byteArray
     * @return
     */
    public static int byteToInt(byte[] byteArray){
        int value;
        value = (int) ((byteArray[0] & 0xFF)
                | ((byteArray[1] << 8) & 0xFF00)
                | ((byteArray[2] << 16) & 0xFF0000)
                | ((byteArray[3] << 24) & 0xFF000000));
        return value;
    }

    public static byte[] readInPos(byte[] source, int pos, int len) {
        byte[] result = new byte[len];
        for (int i = pos,j=0; i < pos + len; i++) {
            result[j++] = source[i];
        }
        return result;
    }
}
