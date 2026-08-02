package com.joysistvi.brgyconnectapp.repository;

import com.joysistvi.brgyconnectapp.config.ConnectionFactory;
import com.joysistvi.brgyconnectapp.model.AccountStatus;
import com.joysistvi.brgyconnectapp.model.User;
import com.joysistvi.brgyconnectapp.model.UserRole;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepoImpl implements UserRepo {
    private static final String USER_COLUMNS = """
            user_id, resident_id, username, password_hash, display_name,
            role, account_status, failed_login_attempts, last_login_at,
            created_at, updated_at
            """;

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
    public List<User> getAll() throws SQLException {
        String sql = "SELECT " + USER_COLUMNS + " FROM users ORDER BY display_name, username";
        return queryUsers(sql);
    }

    @Override
    public List<User> search(String keyword, int maximumRows) throws SQLException {
        String sql = "SELECT " + USER_COLUMNS + """
                FROM users
                WHERE username LIKE ?
                   OR display_name LIKE ?
                   OR role LIKE ?
                   OR account_status LIKE ?
                ORDER BY display_name, username
                LIMIT ?
                """;
        List<User> users = new ArrayList<>();
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            statement.setString(1, pattern);
            statement.setString(2, pattern);
            statement.setString(3, pattern);
            statement.setString(4, pattern);
            statement.setInt(5, maximumRows);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    users.add(mapUser(resultSet));
                }
            }
        }
        return users;
    }

    @Override
    public Optional<User> findById(int userId) throws SQLException {
        String sql = "SELECT " + USER_COLUMNS + " FROM users WHERE user_id = ?";
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapUser(resultSet)) : Optional.empty();
            }
        }
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
    public Optional<User> findByResidentId(int residentId) throws SQLException {
        String sql = "SELECT " + USER_COLUMNS + " FROM users WHERE resident_id = ?";
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, residentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapUser(resultSet)) : Optional.empty();
            }
        }
    }

    @Override
    public boolean save(User user) throws SQLException {
        String sql = """
                INSERT INTO users (
                    resident_id, username, password_hash, display_name,
                    role, account_status, failed_login_attempts
                ) VALUES (?, ?, ?, ?, ?, ?, 0)
                """;
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setObject(1, user.getResidentId());
            statement.setString(2, user.getUsername());
            statement.setString(3, user.getPasswordHash());
            statement.setString(4, user.getDisplayName());
            statement.setString(5, user.getRole().name());
            statement.setString(6, user.getAccountStatus().name());
            if (statement.executeUpdate() == 0) {
                return false;
            }
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setUserId(keys.getInt(1));
                }
            }
            return true;
        }
    }

    @Override
    public boolean updateRole(int userId, UserRole role) throws SQLException {
        String sql = """
                UPDATE users
                SET role = ?,
                    resident_id = CASE WHEN ? = 'RESIDENT' THEN resident_id ELSE NULL END,
                    updated_at = NOW()
                WHERE user_id = ?
                """;
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, role.name());
            statement.setString(2, role.name());
            statement.setInt(3, userId);
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateStatus(int userId, AccountStatus status) throws SQLException {
        String sql = """
                UPDATE users
                SET account_status = ?,
                    failed_login_attempts = CASE WHEN ? = 'ACTIVE' THEN 0 ELSE failed_login_attempts END,
                    updated_at = NOW()
                WHERE user_id = ?
                """;
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            statement.setString(2, status.name());
            statement.setInt(3, userId);
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean unlock(int userId) throws SQLException {
        String sql = """
                UPDATE users
                SET account_status = 'ACTIVE', failed_login_attempts = 0, updated_at = NOW()
                WHERE user_id = ? AND account_status = 'LOCKED'
                """;
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updatePassword(int userId, String passwordHash) throws SQLException {
        String sql = """
                UPDATE users
                SET password_hash = ?, failed_login_attempts = 0, updated_at = NOW()
                WHERE user_id = ?
                """;
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, passwordHash);
            statement.setInt(2, userId);
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public long countActiveAdmins() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'ADMIN' AND account_status = 'ACTIVE'";
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getLong(1);
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

    private List<User> queryUsers(String sql) throws SQLException {
        List<User> users = new ArrayList<>();
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                users.add(mapUser(resultSet));
            }
        }
        return users;
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
