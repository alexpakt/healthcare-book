package com.healthcare.childrecords.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.HexFormat;

@Component
public class AmkaHashUtil {

    private static final String ALGORITHM = "HmacSHA256";
    private static final int MIN_KEY_BYTES = 32;

    private final SecretKeySpec keySpec;

    public AmkaHashUtil(@Value("${app.amka-hash.key}") String keyBase64) {
        if (keyBase64 == null || keyBase64.isBlank()) {
            throw new IllegalStateException("app.amka-hash.key (APP_AMKA_HASH_KEY) must be set");
        }
        byte[] key = Base64.getDecoder().decode(keyBase64);
        if (key.length < MIN_KEY_BYTES) {
            throw new IllegalStateException("app.amka-hash.key must be at least 256 bits");
        }
        this.keySpec = new SecretKeySpec(key, ALGORITHM);
    }

    public String hashAmka(String amka) {
        if (amka == null || amka.isEmpty()) {
            return null;
        }
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(keySpec);
            return HexFormat.of().formatHex(mac.doFinal(amka.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HMAC-SHA256 not available", e);
        }
    }
}
