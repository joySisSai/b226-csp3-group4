package com.barangayconnect.repository;

import com.barangayconnect.config.ConnectionFactory;
import java.sql.Connection;
import java.sql.SQLException;

public final class TransactionManager {
    private final ConnectionFactory connectionFactory;

    public TransactionManager(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public <T> T execute(TransactionWork<T> work) throws SQLException {
        try (Connection connection = connectionFactory.openConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                T result = work.execute(connection);
                connection.commit();
                return result;
            } catch (SQLException | RuntimeException exception) {
                rollback(connection, exception);
                throw exception;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        }
    }

    private static void rollback(Connection connection, Exception original)
            throws SQLException {
        try {
            connection.rollback();
        } catch (SQLException rollbackFailure) {
            original.addSuppressed(rollbackFailure);
        }
    }

    @FunctionalInterface
    public interface TransactionWork<T> {
        T execute(Connection connection) throws SQLException;
    }
}
