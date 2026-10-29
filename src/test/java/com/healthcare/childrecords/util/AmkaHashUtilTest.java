package com.healthcare.childrecords.util;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class AmkaHashUtilTest {

    private static String key(int fill) {
        byte[] k = new byte[32];
        java.util.Arrays.fill(k, (byte) fill);
        return Base64.getEncoder().encodeToString(k);
    }

    @Test
    void sameInputAndKeyGiveSameHash() {
        AmkaHashUtil util = new AmkaHashUtil(key(1));
        assertEquals(util.hashAmka("01019012345"), util.hashAmka("01019012345"));
        assertEquals(64, util.hashAmka("01019012345").length());
    }

    @Test
    void differentKeysGiveDifferentHashes() {
        assertNotEquals(new AmkaHashUtil(key(1)).hashAmka("01019012345"),
                new AmkaHashUtil(key(2)).hashAmka("01019012345"));
    }

    @Test
    void differsFromPlainSha256() throws Exception {
        byte[] sha = java.security.MessageDigest.getInstance("SHA-256").digest("01019012345".getBytes());
        String plain = java.util.HexFormat.of().formatHex(sha);
        assertNotEquals(plain, new AmkaHashUtil(key(1)).hashAmka("01019012345"));
    }

    @Test
    void nullAndEmptyReturnNull() {
        AmkaHashUtil util = new AmkaHashUtil(key(1));
        assertNull(util.hashAmka(null));
        assertNull(util.hashAmka(""));
    }

    @Test
    void rejectsMissingOrShortKey() {
        assertThrows(IllegalStateException.class, () -> new AmkaHashUtil(""));
        assertThrows(IllegalStateException.class,
                () -> new AmkaHashUtil(Base64.getEncoder().encodeToString(new byte[16])));
    }
}
