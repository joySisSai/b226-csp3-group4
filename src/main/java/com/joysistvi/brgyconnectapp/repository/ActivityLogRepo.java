package com.joysistvi.brgyconnectapp.repository;

import com.joysistvi.brgyconnectapp.model.ActivityLog;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ActivityLogRepo {
    boolean save(ActivityLog activityLog) throws SQLException;

    Optional<ActivityLog> getById(long activityLogId) throws SQLException;

    List<ActivityLog> search(Integer userId,
                             String action,
                             String entityType,
                             LocalDate dateFrom,
                             LocalDate dateTo,
                             int maximumRows) throws SQLException;
}
