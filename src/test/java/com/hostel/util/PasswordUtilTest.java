package com.hostel.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordUtilTest {
    @Test
    void usesRandomSaltAndVerifiesOnlyTheMatchingPassword() {
        String first = PasswordUtil.hash("CorrectHorse7");
        String second = PasswordUtil.hash("CorrectHorse7".toCharArray());

        assertNotEquals(first, second);
        assertTrue(PasswordUtil.verify("CorrectHorse7", first));
        assertFalse(PasswordUtil.verify("IncorrectHorse7", first));
        assertFalse(PasswordUtil.verify(null, first));
        assertFalse(PasswordUtil.verify("CorrectHorse7", "not-a-valid-hash"));
    }
}
