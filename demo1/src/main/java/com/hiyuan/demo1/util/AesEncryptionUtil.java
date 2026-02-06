package com.hiyuan.demo1.util;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Component
@Slf4j
public class AesEncryptionUtil {
    
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    
    @Value("${app.encryption.key:}")
    private String encryptionKey;
    
    private SecretKeySpec secretKey;
    
    @PostConstruct
    public void init() {
        if (encryptionKey == null || encryptionKey.isEmpty()) {
            log.warn("APP_ENCRYPTION_KEY not set, using default key. THIS IS NOT SECURE FOR PRODUCTION!");
            encryptionKey = "default-encryption-key-32-chars!";
        }
        
        // 确保密钥长度为32字节(256位)
        String key = encryptionKey.length() > 32 
            ? encryptionKey.substring(0, 32) 
            : String.format("%-32s", encryptionKey).replace(' ', '0');
        
        this.secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
    }
    
    public String encrypt(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            
            // 生成随机IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
            
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            
            // 组合IV + 密文
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encrypted.length);
            byteBuffer.put(iv);
            byteBuffer.put(encrypted);
            
            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            log.error("Failed to encrypt text", e);
            throw new RuntimeException("Encryption failed", e);
        }
    }
    
    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(encryptedText);
            if (decoded.length <= GCM_IV_LENGTH) {
                log.warn("Decoded text shorter than IV, returning original text");
                return encryptedText;
            }
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(decoded, 0, iv, 0, iv.length);
            byte[] cipherText = new byte[decoded.length - GCM_IV_LENGTH];
            System.arraycopy(decoded, GCM_IV_LENGTH, cipherText, 0, cipherText.length);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
            byte[] decrypted = cipher.doFinal(cipherText);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException iae) {
            log.warn("Input text is not valid Base64, returning original text");
            return encryptedText;
        } catch (Exception e) {
            log.error("Failed to decrypt text", e);
            throw new RuntimeException("Decryption failed", e);
        }
    }
}
