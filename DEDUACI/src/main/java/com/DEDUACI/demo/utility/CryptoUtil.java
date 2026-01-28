package com.DEDUACI.demo.utility;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

public class CryptoUtil {

    private static final String ALGORITHM = "AES";

    private static SecretKeySpec getKey(String passcode) throws Exception {
        byte[] key = passcode.getBytes(StandardCharsets.UTF_8);
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        key = sha.digest(key);
        return new SecretKeySpec(Arrays.copyOf(key, 16), ALGORITHM);
    }

    public static byte[] encrypt(byte[] data, String passcode) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, getKey(passcode));
        return cipher.doFinal(data);
    }

    public static byte[] decrypt(byte[] data, String passcode) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, getKey(passcode));
        return cipher.doFinal(data);
    }
}
