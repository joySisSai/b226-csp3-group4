package com.barangayconnect.config;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface ConnectionFactory {
    Connection openConnection() throws SQLException;
}
