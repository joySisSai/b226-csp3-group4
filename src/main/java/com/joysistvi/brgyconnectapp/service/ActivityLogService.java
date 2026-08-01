package com.joysistvi.brgyconnectapp.service;

import com.joysistvi.brgyconnectapp.model.ActivityLog;
import com.joysistvi.brgyconnectapp.repository.ActivityLogRepo;
import com.joysistvi.brgyconnectapp.repository.ActivityLogRepoImpl;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Business logic layer for activity logging.
 * Validates input before passing to repository.
 */
public class ActivityLogService {
    private final ActivityLogRepo repo = new ActivityLogRepoImpl();

    public String logActivity(Integer userId, String action, String entityType, Long entityId, String description) {
        // Validate required fields
        if (userId == null || action == null || action.isBlank())
            return "Required fields missing";

        ActivityLog log = new ActivityLog();
        log.setUserId(userId);
        log.setAction(action);
        // Default to "General" if no module specified
        log.setEntityType(entityType == null || entityType.isBlank() ? "General" : entityType);
        log.setEntityId(entityId);
        log.setDescription(description == null || description.isBlank() ? "No details provided" : description);
        log.setCreatedAt(LocalDateTime.now());

        return repo.save(log) ? "Activity logged successfully" : "Failed to log activity";
    }

    // Return complete audit trail
    public List<ActivityLog> getAllActivityLogs() { return repo.getAll(); }
    public List<ActivityLog> getLogsByUserId(Integer userId) { return repo.getByUser(userId); }
    public List<ActivityLog> getLogsByDateRange(LocalDateTime start, LocalDateTime end) { return repo.getByDateRange(start, end); }
}