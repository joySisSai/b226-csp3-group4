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

    public List<ActivityLog> search(Integer userId,
                                    String action,
                                    String entityType,
                                    LocalDate dateFrom,
                                    LocalDate dateTo) {
        return activityLogService.search(userId, action, entityType, dateFrom, dateTo);
    }

    public ActivityLog getById(long activityLogId) {
        return activityLogService.getById(activityLogId);
    }
}
