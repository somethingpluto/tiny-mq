package org.tiny.mq.nameserver.utils;

import org.tiny.mq.nameserver.config.GlobalConfig;

public class NameServerUtils {

    public static final String verifyReqAttrName = "reqID";

    public static boolean isVerify(String user, String password) {
        String nameserverUser = GlobalConfig.getNameserverConfig().getNameserverUser();
        String nameserverPassword = GlobalConfig.getNameserverConfig().getNameserverPassword();
        return nameserverUser.equals(user) && nameserverPassword.equals(password);
    }


}
