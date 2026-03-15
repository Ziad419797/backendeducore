package com.educore.auth;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

@Component
public class HashUtil {

    public  static String sha256(String value) {
        return DigestUtils.sha256Hex(value);
    }
}
