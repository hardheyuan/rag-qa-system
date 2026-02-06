package com.hiyuan.demo1.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class AesEncryptionUtilTest {

    private AesEncryptionUtil encryptionUtil;

    @BeforeEach
    void setUp() {
        encryptionUtil = new AesEncryptionUtil();
        ReflectionTestUtils.setField(encryptionUtil, "encryptionKey", "test-encryption-key-32-char!!");
        encryptionUtil.init();
    }

    @Test
    void decryptReturnsOriginalForPlainTextInput() {
        String plain = "sk-plain-key-123";

        assertEquals(plain, encryptionUtil.decrypt(plain));
    }

    @Test
    void encryptThenDecryptRoundTrip() {
        String secret = "sk-secret-key-xyz";

        String encrypted = encryptionUtil.encrypt(secret);

        assertNotEquals(secret, encrypted);
        assertEquals(secret, encryptionUtil.decrypt(encrypted));
    }
}
