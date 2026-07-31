package com.joysistvi.brgyconnectapp.repository;

import com.joysistvi.brgyconnectapp.config.ConnectionFactory;
import com.joysistvi.brgyconnectapp.model.AccountStatus;
import com.joysistvi.brgyconnectapp.model.User;
import com.joysistvi.brgyconnectapp.model.UserRole;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

public class UserRepoImpl implements UserRepo {
    private static final String FIND_BY_USERNAME_SQL = """
            SELECT user_id, resident_id, username, password_hash, display_name,
                   role, account_status, failed_login_attempts, last_login_at,
                   created_at, updated_at
            FROM users
            WHERE username = ?
            LIMIT 1
            """;

    private static final String RECORD_FAILED_LOGIN_SQL = """
            UPDATE users
            SET account_status = CASE
                    WHEN failed_login_attempts + 1 >= ? THEN 'LOCKED'
                    ELSE account_status
                END,
                failed_login_attempts = failed_login_attempts + 1
            WHERE user_id = ?
            """;

    private static final String RECORD_SUCCESSFUL_LOGIN_SQL = """
            UPDATE users
            SET failed_login_attempts = 0,
                last_login_at = CURRENT_TIMESTAMP
            WHERE user_id = ?
            """;

    private final ConnectionFactory connectionFactory;

    public UserRepoImpl(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Optional<User> findByUsername(String username) throws SQLException {
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_USERNAME_SQL)) {
            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(mapUser(resultSet))
                        : Optional.empty();
            }
        }
    }

    @Override
    public void recordFailedLogin(int userId, int maximumAttempts) throws SQLException {
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(RECORD_FAILED_LOGIN_SQL)) {
            statement.setInt(1, maximumAttempts);
            statement.setInt(2, userId);
            statement.executeUpdate();
        }
    }

    @Override
    public void recordSuccessfulLogin(int userId) throws SQLException {
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(RECORD_SUCCESSFUL_LOGIN_SQL)) {
            statement.setInt(1, userId);
            statement.executeUpdate();
        }
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setUserId(resultSet.getInt("user_id"));
        user.setResidentId(resultSet.getObject("resident_id", Integer.class));
        user.setUsername(resultSet.getString("username"));
        user.setPasswordHash(resultSet.getString("password_hash"));
        user.setDisplayName(resultSet.getString("display_name"));
        user.setRole(UserRole.valueOf(resultSet.getString("role")));
        user.setAccountStatus(AccountStatus.valueOf(resultSet.getString("account_status")));
        user.setFailedLoginAttempts(resultSet.getInt("failed_login_attempts"));
        user.setLastLoginAt(toLocalDateTime(resultSet.getTimestamp("last_login_at")));
        user.setCreatedAt(toLocalDateTime(resultSet.getTimestamp("created_at")));
        user.setUpdatedAt(toLocalDateTime(resultSet.getTimestamp("updated_at")));
        return user;
    }

    private java.time.LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
