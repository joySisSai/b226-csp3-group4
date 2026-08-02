package com.joysistvi.brgyconnectapp.service;

import java.sql.SQLException;

public final class DatabaseErrors {
    private DatabaseErrors() {
    }

    public static boolean isConstraintViolation(SQLException exception) {
        String sqlState = exception.getSQLState();
        return sqlState != null && sqlState.startsWith("23");
    }
}
