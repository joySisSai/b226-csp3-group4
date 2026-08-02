package com.joysistvi.brgyconnectapp.controller;

import com.joysistvi.brgyconnectapp.model.ActivityLog;
import com.joysistvi.brgyconnectapp.service.ActivityLogService;

import java.time.LocalDate;
import java.util.List;

public class ActivityLogController {
    private final ActivityLogService activityLogService;

    public ActivityLogController(ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }

    public List<ActivityLog> search(int actingAdminId,
                                    Integer userId,
                                    String action,
                                    String entityType,
                                    LocalDate dateFrom,
                                    LocalDate dateTo,
                                    int offset,
                                    int limit) {
        return activityLogService.search(actingAdminId, userId, action, entityType, dateFrom, dateTo, offset, limit);
    }

    public ActivityLog getById(long activityLogId, int actingAdminId) {
        return activityLogService.getById(activityLogId, actingAdminId);
    }
}
