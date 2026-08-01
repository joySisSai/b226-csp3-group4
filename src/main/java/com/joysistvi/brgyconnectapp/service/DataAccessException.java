package com.joysistvi.brgyconnectapp.service;

import java.sql.SQLException;

public class DataAccessException extends RuntimeException {
    public static final String USER_MESSAGE =
            "Unable to load data because the database is unavailable. Please try again.";

    public DataAccessException(SQLException cause) {
        super(USER_MESSAGE, cause);
    }
}
