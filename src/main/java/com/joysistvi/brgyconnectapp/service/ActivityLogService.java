package com.joysistvi.brgyconnectapp.service;

import com.joysistvi.brgyconnectapp.model.ActivityLog;
import com.joysistvi.brgyconnectapp.repository.ActivityLogRepo;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public class ActivityLogService {
    private static final int MAXIMUM_RESULTS = 100;
    private final ActivityLogRepo activityLogRepo;

    public ActivityLogService(ActivityLogRepo activityLogRepo) {
        this.activityLogRepo = activityLogRepo;
    }

    public boolean record(Integer userId,
                          String action,
                          String entityType,
                          Long entityId,
                          String description) {
        if (isBlank(action) || isBlank(entityType)) {
            return false;
        }

        ActivityLog activityLog = new ActivityLog();
        activityLog.setUserId(userId != null && userId > 0 ? userId : null);
        activityLog.setAction(normalizeCategory(action));
        activityLog.setEntityType(normalizeCategory(entityType));
        activityLog.setEntityId(entityId != null && entityId > 0 ? entityId : null);
        activityLog.setDescription(normalizeDescription(description));
        try {
            return activityLogRepo.save(activityLog);
        } catch (SQLException exception) {
            return false;
        }
    }

    public List<ActivityLog> search(Integer userId,
                                    String action,
                                    String entityType,
                                    LocalDate dateFrom,
                                    LocalDate dateTo) {
        if (userId != null && userId <= 0) {
            return List.of();
        }
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            return List.of();
        }
        try {
            return activityLogRepo.search(
                    userId,
                    normalizeOptionalCategory(action),
                    normalizeOptionalCategory(entityType),
                    dateFrom,
                    dateTo,
                    MAXIMUM_RESULTS
            );
        } catch (SQLException exception) {
            return List.of();
        }
    }

    public ActivityLog getById(long activityLogId) {
        if (activityLogId <= 0) {
            return null;
        }
        try {
            return activityLogRepo.getById(activityLogId).orElse(null);
        } catch (SQLException exception) {
            return null;
        }
    }

    private String normalizeCategory(String value) {
        String normalized = value.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
        return normalized.length() <= 50 ? normalized : normalized.substring(0, 50);
    }

    private String normalizeOptionalCategory(String value) {
        return isBlank(value) ? null : normalizeCategory(value);
    }

    private String normalizeDescription(String value) {
        if (isBlank(value)) {
            return null;
        }
        String normalized = value.trim();
        return normalized.length() <= 1000 ? normalized : normalized.substring(0, 1000);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
