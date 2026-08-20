package com.example.study11.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AESUtilTest {

    private static final String AES_KEY = "0000000000000000";

    @Test
    void shouldEncryptAndDecryptWithProvidedKey() {
        String content = "1:1723536000000";

        String encrypted = AESUtil.encrypt(content, AES_KEY);
        String decrypted = AESUtil.decrypt(encrypted, AES_KEY);

        assertEquals(content, decrypted);
    }
}
