package org.tiny.mq.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;

public class FileContentUtil {

    /**
     * 读取指定路径下文件内容
     * @param path
     * @return
     */
    public static String readFromFile(String path){
        try(BufferedReader in = new BufferedReader(new FileReader(path))) {
            StringBuilder stringBuffer = new StringBuilder();
            while (in.ready()){
                stringBuffer.append(in.readLine());
            }
            return stringBuffer.toString();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     * 向指定路径写入文件
     * @param path
     * @param content
     */
    public static void overWriteToFile(String path,String content){
        try(FileWriter fileWriter = new FileWriter(path)) {
            fileWriter.write(content);
            fileWriter.flush();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
