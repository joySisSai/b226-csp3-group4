package com.joysistvi.brgyconnectapp.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ResidentFieldValidatorTest {

    @Test
    public void testGenerateResidentCode_Valid() {
        String code = ResidentFieldValidator.generateResidentCode(1, 2026);
        assertEquals("RES-2026-000001", code);
    }

    @Test
    public void testGenerateResidentCode_InvalidId() {
        assertThrows(IllegalArgumentException.class, () -> {
            ResidentFieldValidator.generateResidentCode(0, 2026);
        });
    }

    @Test
    public void testGenerateResidentCode_InvalidYear() {
        assertThrows(IllegalArgumentException.class, () -> {
            ResidentFieldValidator.generateResidentCode(1, 10000);
        });
    }
}
