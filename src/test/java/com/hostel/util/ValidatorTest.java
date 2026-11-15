package com.hostel.util;

import com.hostel.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidatorTest {
    @Test
    void validatesRequiredEmailPhoneUsnAndPasswordFields() {
        Validator validator = new Validator()
                .required("name", " ", "Name")
                .email("email", "bad-email")
                .phone("phone", "123")
                .usn("usn", "short")
                .password("password", "allletters");

        assertTrue(validator.hasErrors());
        ValidationException exception = assertThrows(ValidationException.class, validator::throwIfInvalid);
        assertEquals(5, exception.getErrors().size());
    }

    @Test
    void acceptsValidInputs() {
        Validator validator = new Validator()
                .required("name", "Student", "Name")
                .email("email", "student@example.com")
                .phone("phone", "9876543210")
                .usn("usn", "1RV23CS001")
                .password("password", "strongpass7");

        validator.throwIfInvalid();
    }
}
