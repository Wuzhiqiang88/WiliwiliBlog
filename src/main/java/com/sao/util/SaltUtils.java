package com.sao.util;

import java.security.SecureRandom;

/**
 * 获取随机salt
 */
public class SaltUtils {

    public static String getSalt(){
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[15];
        random.nextBytes(bytes);
        String salt = org.apache.commons.codec.binary.Base64.encodeBase64String(bytes);
        return salt;
    }
}
