package org.tiny.mq.utils;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.tiny.mq.model.commitlog.TopicModel;

import java.util.List;

public class TestUtils {
    @Test
    public void TestFileContentUtil(){
        String content = FileContentUtil.readFromFile("E:\\code\\code_back\\Java_project\\tiny-frame\\tinymq\\broker\\config\\tinymq-topic.json");
        System.out.println(content);
        List<TopicModel> topicModels = JSON.parseArray(content, TopicModel.class);
        System.out.println(topicModels);
    }
}
