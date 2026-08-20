package com.example.study11.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * AES 加密工具类
 */
public class AESUtil {

    // 算法/模式/填充
    private static final String ALGORITHM = "AES/ECB/PKCS5Padding";

    /*
     * AES 加密
     *
     * @param content 待加密内容
     * @param aesKey  从登录配置读取的 AES 密钥
     * @return Base64 编码的加密字符串
     */
    public static String encrypt(String content, String aesKey) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(aesKey.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("AES加密失败", e);
        }
    }

    /**
     * AES 解密
     *
     * @param encryptedContent Base64 编码的加密内容
     * @param aesKey           从登录配置读取的 AES 密钥
     * @return 解密后的字符串
     */
    public static String decrypt(String encryptedContent, String aesKey) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(aesKey.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedContent);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("AES解密失败", e);
        }
    }

}
