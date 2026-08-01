package com.joysistvi.brgyconnectapp.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnection implements ConnectionFactory {
    private final DatabaseConfig databaseConfig;

    public DbConnection(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    @Override
    public Connection openConnection() throws SQLException {
        return DriverManager.getConnection(
                databaseConfig.url(),
                databaseConfig.username(),
                databaseConfig.password()
        );
    }
}
