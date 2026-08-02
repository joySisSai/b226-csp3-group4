package com.joysistvi.brgyconnectapp.config;

import java.util.ArrayList;
import java.util.List;

public record DatabaseConfig(String url, String username, String password) {
    private static final String URL_ENV = "DB_URL";
    private static final String USERNAME_ENV = "DB_USERNAME";
    private static final String PASSWORD_ENV = "DB_PASSWORD";

    public DatabaseConfig {
        url = trimToNull(url);
        username = trimToNull(username);
        password = password == null ? "" : password;

        List<String> errors = new ArrayList<>();
        if (url == null) {
            errors.add(URL_ENV);
        } else if (!url.startsWith("jdbc:mysql://")) {
            errors.add(URL_ENV + " must be a MySQL JDBC URL");
        }
        if (username == null) {
            errors.add(USERNAME_ENV);
        }
        if (!errors.isEmpty()) {
            throw new IllegalStateException(
                    "Invalid database configuration: " + String.join(", ", errors));
        }
    }

    public static DatabaseConfig load() {
        return new DatabaseConfig(
                System.getenv(URL_ENV),
                System.getenv(USERNAME_ENV),
                System.getenv(PASSWORD_ENV)
        );
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
