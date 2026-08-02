package com.joysistvi.brgyconnectapp.repository;

import com.joysistvi.brgyconnectapp.config.ConnectionFactory;
import com.joysistvi.brgyconnectapp.model.ActivityLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ActivityLogRepoImpl implements ActivityLogRepo {
    private final ConnectionFactory connectionFactory;

    public ActivityLogRepoImpl(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public boolean save(ActivityLog activityLog) throws SQLException {
        String sql = """
                INSERT INTO activity_logs (
                    user_id, action, entity_type, entity_id, description
                ) VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (activityLog.getUserId() == null) {
                statement.setNull(1, Types.INTEGER);
            } else {
                statement.setInt(1, activityLog.getUserId());
            }
            statement.setString(2, activityLog.getAction());
            statement.setString(3, activityLog.getEntityType());
            if (activityLog.getEntityId() == null) {
                statement.setNull(4, Types.BIGINT);
            } else {
                statement.setLong(4, activityLog.getEntityId());
            }
            if (activityLog.getDescription() == null) {
                statement.setNull(5, Types.VARCHAR);
            } else {
                statement.setString(5, activityLog.getDescription());
            }
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public Optional<ActivityLog> getById(long activityLogId) throws SQLException {
        String sql = """
                SELECT al.activity_log_id, al.user_id, al.action, al.entity_type,
                       al.entity_id, al.description, al.created_at,
                       u.username AS actor_username, u.display_name AS actor_display_name
                FROM activity_logs al
                LEFT JOIN users u ON u.user_id = al.user_id
                WHERE al.activity_log_id = ?
                """;
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, activityLogId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(mapActivityLog(resultSet))
                        : Optional.empty();
            }
        }
    }

    @Override
    public List<ActivityLog> search(Integer userId,
                                    String action,
                                    String entityType,
                                    LocalDate dateFrom,
                                    LocalDate dateTo,
                                    int offset,
                                    int limit) throws SQLException {
        StringBuilder sql = new StringBuilder("""
                SELECT al.activity_log_id, al.user_id, al.action, al.entity_type,
                       al.entity_id, al.description, al.created_at,
                       u.username AS actor_username, u.display_name AS actor_display_name
                FROM activity_logs al
                LEFT JOIN users u ON u.user_id = al.user_id
                WHERE 1 = 1
                """);
        List<Object> parameters = new ArrayList<>();

        if (userId != null) {
            sql.append(" AND al.user_id = ?");
            parameters.add(userId);
        }
        if (action != null) {
            sql.append(" AND al.action = ?");
            parameters.add(action);
        }
        if (entityType != null) {
            sql.append(" AND al.entity_type = ?");
            parameters.add(entityType);
        }
        if (dateFrom != null) {
            sql.append(" AND al.created_at >= ?");
            parameters.add(Timestamp.valueOf(dateFrom.atStartOfDay()));
        }
        if (dateTo != null) {
            sql.append(" AND al.created_at < ?");
            parameters.add(Timestamp.valueOf(dateTo.plusDays(1).atStartOfDay()));
        }
        sql.append(" ORDER BY al.created_at DESC, al.activity_log_id DESC LIMIT ? OFFSET ?");
        parameters.add(limit);
        parameters.add(offset);

        List<ActivityLog> logs = new ArrayList<>();
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            for (int index = 0; index < parameters.size(); index++) {
                statement.setObject(index + 1, parameters.get(index));
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    logs.add(mapActivityLog(resultSet));
                }
            }
        }
        return logs;
    }

    private ActivityLog mapActivityLog(ResultSet resultSet) throws SQLException {
        ActivityLog activityLog = new ActivityLog();
        activityLog.setActivityLogId(resultSet.getLong("activity_log_id"));
        activityLog.setUserId(resultSet.getObject("user_id", Integer.class));
        activityLog.setActorUsername(resultSet.getString("actor_username"));
        activityLog.setActorDisplayName(resultSet.getString("actor_display_name"));
        activityLog.setAction(resultSet.getString("action"));
        activityLog.setEntityType(resultSet.getString("entity_type"));
        activityLog.setEntityId(resultSet.getObject("entity_id", Long.class));
        activityLog.setDescription(resultSet.getString("description"));
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        activityLog.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());
        return activityLog;
    }
}
