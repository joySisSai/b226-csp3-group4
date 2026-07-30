package com.barangayconnect.repository.contracts;

import com.barangayconnect.model.records.ActivityLog;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface ActivityLogRepository {
    ActivityLog save(ActivityLog activityLog) throws SQLException;
    ActivityLog save(Connection connection, ActivityLog activityLog) throws SQLException;
    List<ActivityLog> findByUserId(int userId) throws SQLException;
}
