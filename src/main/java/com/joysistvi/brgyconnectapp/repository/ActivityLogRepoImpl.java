package com.joysistvi.brgyconnectapp.repository;

import com.joysistvi.brgyconnectapp.config.ConnectionFactory;
import com.joysistvi.brgyconnectapp.config.DbConnection;
import com.joysistvi.brgyconnectapp.model.ActivityLog;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogRepoImpl implements ActivityLogRepo {
    private final ConnectionFactory dbFactory = new DbConnection();

    @Override
    public boolean save(ActivityLog log) {
        // Insert new log entry
        String sql = "INSERT INTO activity_logs (user_id, action, entity_type, entity_id, description, created_at) VALUES (?,?,?,?,?, NOW())";
        try (Connection conn = dbFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, log.getUserId());
            stmt.setString(2, log.getAction());
            stmt.setString(3, log.getEntityType());
            stmt.setObject(4, log.getEntityId());
            stmt.setString(5, log.getDescription());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error saving activity log: " + e.getMessage());
        }
        return false;
    }

    @Override
    public List<ActivityLog> getAll() {
        List<ActivityLog> list = new ArrayList<>();
        // Order by newest first
        String sql = "SELECT * FROM activity_logs ORDER BY created_at DESC";
        try (Connection conn = dbFactory.openConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapToLog(rs));
        } catch (SQLException e) {
            System.err.println("Error fetching all logs: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<ActivityLog> getByUser(Integer userId) {
        List<ActivityLog> list = new ArrayList<>();
        String sql = "SELECT * FROM activity_logs WHERE user_id = ? ORDER BY created_at DESC";
        try (Connection conn = dbFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapToLog(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user logs: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<ActivityLog> getByDateRange(LocalDateTime start, LocalDateTime end) {
        List<ActivityLog> list = new ArrayList<>();
        String sql = "SELECT * FROM activity_logs WHERE created_at BETWEEN ? AND ? ORDER BY created_at DESC";
        try (Connection conn = dbFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(start));
            stmt.setTimestamp(2, Timestamp.valueOf(end));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapToLog(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching logs by range: " + e.getMessage());
        }
        return list;
    }

    // Map database result set to ActivityLog model object
    private ActivityLog mapToLog(ResultSet rs) throws SQLException {
        ActivityLog log = new ActivityLog();
        log.setActivityLogId(rs.getObject("activity_log_id", Long.class));
        log.setUserId(rs.getObject("user_id", Integer.class));
        log.setAction(rs.getString("action"));
        log.setEntityType(rs.getString("entity_type"));
        log.setEntityId(rs.getObject("entity_id", Long.class));
        log.setDescription(rs.getString("description"));
        Timestamp t = rs.getTimestamp("created_at");
        if (t != null) log.setCreatedAt(t.toLocalDateTime());
        return log;
    }
}