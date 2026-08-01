package com.joysistvi.brgyconnectapp.config;

import java.util.ArrayList;
import java.util.List;

public record DatabaseConfig(String url, String username, String password) {
    private static final String URL_ENV = "BARANGAYCONNECT_DB_URL";
    private static final String USERNAME_ENV = "BARANGAYCONNECT_DB_USERNAME";
    private static final String PASSWORD_ENV = "BARANGAYCONNECT_DB_PASSWORD";

    private static final String URL_PROPERTY = "barangayconnect.db.url";
    private static final String USERNAME_PROPERTY = "barangayconnect.db.username";
    private static final String PASSWORD_PROPERTY = "barangayconnect.db.password";

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
                externalValue(URL_PROPERTY, URL_ENV),
                externalValue(USERNAME_PROPERTY, USERNAME_ENV),
                externalValue(PASSWORD_PROPERTY, PASSWORD_ENV)
        );
    }

    private static String externalValue(String propertyName, String environmentName) {
        String propertyValue = System.getProperty(propertyName);
        return propertyValue != null ? propertyValue : System.getenv(environmentName);
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
