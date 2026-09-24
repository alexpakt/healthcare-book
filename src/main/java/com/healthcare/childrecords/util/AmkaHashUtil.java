package com.healthcare.childrecords.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AmkaHashUtil {

    private static final String ALGORITHM = "SHA-256";

    public static String hashAmka(String amka) {
        if (amka == null || amka.isEmpty()) {
            return null;
        }

        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] hash = digest.digest(amka.getBytes());
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
