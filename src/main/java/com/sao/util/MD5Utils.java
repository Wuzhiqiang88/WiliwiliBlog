package com.sao.util;

import com.google.common.hash.Hashing;
import org.apache.commons.codec.Charsets;
import org.apache.shiro.crypto.hash.SimpleHash;

public class MD5Utils {

    public static String getMD5Code(String content) {
        return Hashing.md5().newHasher().putString(content, Charsets.UTF_8).hash().toString();
    }

    public static String getEncryptedPassword(String salt,String password){
        String hashAlgorithmName = "md5";//加密类型
        Integer iteration = 2;//迭代次数
        String encryptedPassword = new SimpleHash(hashAlgorithmName,password,salt,iteration).toHex();
        return encryptedPassword;
    }
}
