package com.barangayconnect.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnection implements ConnectionFactory {
    private final static String URL = "jdbc:mysql://localhost:3306/barangayconnect_db";
    private final static String USERNAME = "root";
    private final static String PASSWORD = "";

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    @Override
    public Connection openConnection() throws SQLException {
        return connect();
    }
}
