package com.joysistvi.brgyconnectapp.validation;

import java.time.LocalDate;
import java.util.regex.Pattern;

public final class ResidentFieldValidator {
    private static final int MAXIMUM_AGE_YEARS = 120;
    private static final int MAXIMUM_EMAIL_LENGTH = 120;
    private static final Pattern CONTACT_NUMBER_PATTERN =
            Pattern.compile("^(?:09\\d{9}|\\+639\\d{9})$");
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private ResidentFieldValidator() {
    }

    public static String validateBirthDate(LocalDate birthDate) {
        if (birthDate == null) {
            return "Birth date is required";
        }

        LocalDate today = LocalDate.now();
        if (birthDate.isAfter(today)) {
            return "Birth date cannot be in the future";
        }
        if (birthDate.isBefore(today.minusYears(MAXIMUM_AGE_YEARS))) {
            return "Birth date cannot be more than " + MAXIMUM_AGE_YEARS + " years ago";
        }
        return null;
    }

    public static String validateContactNumber(String contactNumber) {
        String normalized = normalizeOptional(contactNumber);
        if (normalized == null) {
            return null;
        }
        return CONTACT_NUMBER_PATTERN.matcher(normalized).matches()
                ? null
                : "Contact number must use 09XXXXXXXXX or +639XXXXXXXXX format";
    }

    public static String validateEmail(String email) {
        String normalized = normalizeOptional(email);
        if (normalized == null) {
            return null;
        }
        if (normalized.length() > MAXIMUM_EMAIL_LENGTH) {
            return "Email address cannot exceed " + MAXIMUM_EMAIL_LENGTH + " characters";
        }
        return EMAIL_PATTERN.matcher(normalized).matches()
                ? null
                : "Enter a valid email address, such as name@example.com";
    }

    public static String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
