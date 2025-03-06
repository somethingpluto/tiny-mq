package org.tiny.mq.nameserver.utils;


import org.tiny.mq.nameserver.common.CommonCache;

public class NameserverUtils {

    public static boolean isVerify(String user, String password) {
        String rightUser = CommonCache.getNameserverProperties().getNameserverUser();
        String rightPassword = CommonCache.getNameserverProperties().getNameserverPwd();
        if (!rightUser.equals(user) || !rightPassword.equals(password)) {
            return false;
        }
        return true;
    }

}
