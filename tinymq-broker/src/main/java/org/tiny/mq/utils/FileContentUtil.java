package org.tiny.mq.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class FileContentUtil {

    public static String readFromFile(String path) {
        try (BufferedReader in = new BufferedReader(new FileReader(path))) {
            StringBuffer stb = new StringBuffer();
            while (in.ready()) {
                stb.append(in.readLine());
            }
            return stb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void overWriteToFile(String path, String content) {
        try (FileWriter fileWriter = new FileWriter(path)) {
            fileWriter.write(content);
            fileWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

//    public static void main(String[] args) {
//        String content = FileContentReaderUtil.readFromFile("/Users/linhao/IdeaProjects-new/eaglemq/broker/config/eaglemq-topic.json");
//        System.out.println(content);
//        List<EagleMqTopicModel> eagleMqTopicModelList = JSON.parseArray(content, EagleMqTopicModel.class);
//        System.out.println(eagleMqTopicModelList);
//    }
}
