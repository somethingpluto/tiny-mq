package org.tiny.mq.utils;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;

import java.util.concurrent.atomic.AtomicInteger;
import java.lang.reflect.Type;

public class AtomicIntegerDeserializer implements ObjectDeserializer {

    public <T> T deserialze(DefaultJSONParser defaultJSONParser, Type type, Object o) {
        // 解析当前字段的值，假设该值是一个数字
        Object value = defaultJSONParser.parse();
        if (value instanceof Number) {
            return (T) new AtomicInteger(((Number) value).intValue());
        }
        return null;
    }

    @Override
    public int getFastMatchToken() {
        return 0;  // 默认值，可以根据需要设置为其他标记
    }

    public static void main(String[] args) {
        // 注册自定义的反序列化器
        ParserConfig.getGlobalInstance().putDeserializer(AtomicInteger.class, new AtomicIntegerDeserializer());

        // 测试反序列化
        String json = "{\"count\": 123}";
        MyClass myClass = JSON.parseObject(json, MyClass.class);
        System.out.println(myClass.getCount().get());  // 输出：123
    }
}

class MyClass {
    private AtomicInteger count;

    public AtomicInteger getCount() {
        return count;
    }

    public void setCount(AtomicInteger count) {
        this.count = count;
    }
}
