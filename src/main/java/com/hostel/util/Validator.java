package com.hostel.util;

import com.hostel.exception.ValidationException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public final class Validator {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern PHONE = Pattern.compile("^\\d{10}$");
    private static final Pattern USN = Pattern.compile("^[A-Za-z0-9]{8,20}$");
    private static final Pattern PASSWORD_LETTER = Pattern.compile(".*[A-Za-z].*");
    private static final Pattern PASSWORD_DIGIT = Pattern.compile(".*\\d.*");

    private final Map<String, String> errors = new LinkedHashMap<>();

    public Validator required(String field, String value, String label) {
        if (value == null || value.isBlank()) {
            errors.putIfAbsent(field, label + " is required.");
        }
        return this;
    }

    public Validator maxLength(String field, String value, int maxLength, String label) {
        if (value != null && value.length() > maxLength) {
            errors.putIfAbsent(field, label + " must be at most " + maxLength + " characters.");
        }
        return this;
    }

    public Validator email(String field, String value) {
        if (value != null && !value.isBlank() && !EMAIL.matcher(value.trim()).matches()) {
            errors.putIfAbsent(field, "Enter a valid email address.");
        }
        return this;
    }

    public Validator phone(String field, String value) {
        if (value != null && !PHONE.matcher(value.trim()).matches()) {
            errors.putIfAbsent(field, "Phone number must contain exactly 10 digits.");
        }
        return this;
    }

    public Validator usn(String field, String value) {
        if (value != null && !value.isBlank() && !USN.matcher(value.trim()).matches()) {
            errors.putIfAbsent(field, "Enter a valid USN using 8 to 20 letters or digits.");
        }
        return this;
    }

    public Validator password(String field, String value) {
        if (value == null || value.length() < 8
                || !PASSWORD_LETTER.matcher(value).matches()
                || !PASSWORD_DIGIT.matcher(value).matches()) {
            errors.putIfAbsent(field, "Password must be at least 8 characters and include a letter and a digit.");
        }
        return this;
    }

    public Validator custom(String field, boolean valid, String message) {
        if (!valid) {
            errors.putIfAbsent(field, message);
        }
        return this;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public Map<String, String> getErrors() {
        return Map.copyOf(errors);
    }

    public void throwIfInvalid() {
        if (hasErrors()) {
            throw new ValidationException(errors);
        }
    }

    public static void validate(Map<String, String> errors) {
        if (errors == null || errors.isEmpty()) {
            return;
        }
        throw new ValidationException(errors);
    }
}
