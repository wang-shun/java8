/*
 * Copyright (c) 2015 杭州端点网络科技有限公司
 */

package io.terminus.doctor.web.core.util;

import com.google.common.base.Optional;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.Security;
import java.util.Arrays;

/**
 * @author Effet
 */
public final class SimpleAESUtils {

    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    // 将密钥 padding 成 32-Byte (256-bit)
    private static Key genKey(String password) throws Exception {
        return new SecretKeySpec(Arrays.copyOf(password.getBytes("UTF-8"), 32), "AES");
    }

    public static String encrypt(String data, String password, String alg) throws Exception {
        Key key = genKey(password);
        Cipher cipher = Cipher.getInstance(alg, "BC");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encrypted = cipher.doFinal(data.getBytes());
        return Hex.encodeHexString(encrypted);
    }

    public static String decrypt(String encryptedData, String password, String alg) throws Exception {
        Key key = genKey(password);
        Cipher cipher = Cipher.getInstance(alg, "BC");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] encrypted = Hex.decodeHex(encryptedData.toCharArray());
        return new String(cipher.doFinal(encrypted));
    }

    /**
     * 支持可选 padding 方法
     *
     * @param padding 目前只支持 pkcs5 和 pkcs7
     * @return 标准化的算法名
     */
    public static Optional<String> algSelect(String padding) {
        if (padding == null || "pkcs5".equals(padding)) {
            return Optional.of("AES/ECB/PKCS5Padding");
        }
        if ("pkcs7".equals(padding)) {
            return Optional.of("AES/ECB/PKCS7Padding");
        }
        return Optional.absent();
    }
}
