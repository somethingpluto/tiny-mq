package org.tiny.mq.common.utils;

import java.util.List;


public class AssertUtils {


    public static void isNotBlank(String val, String msg) {
        if (val == null || val.trim().length() == 0) {
            throw new RuntimeException(msg);
        }
    }

    public static void isNotEmpty(List list, String msg) {
        if (list == null || list.isEmpty()) {
            throw new RuntimeException(msg);
        }
    }


    public static void isNotNull(Object val, String msg) {
        if (val == null) {
            throw new RuntimeException(msg);
        }
    }

    public static void isTrue(Boolean condition, String msg) {
        if (!condition) {
            throw new RuntimeException(msg);
        }
    }
}
