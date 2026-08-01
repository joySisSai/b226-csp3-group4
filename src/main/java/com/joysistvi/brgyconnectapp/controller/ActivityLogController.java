package com.joysistvi.brgyconnectapp.controller;

import com.joysistvi.brgyconnectapp.model.ActivityLog;
import com.joysistvi.brgyconnectapp.service.ActivityLogService;

import java.time.LocalDateTime;
import java.util.List;

public class ActivityLogController {
    private final ActivityLogService service;

    public ActivityLogController() {
        this.service = new ActivityLogService();
    }

    // Record an action from any part of the system
    public String recordLog(Integer userId, String action, String entityType, Long entityId, String description) {
        return service.logActivity(userId, action, entityType, entityId, description);
    }

    // View all logs
    public List<ActivityLog> viewAllLogs() { return service.getAllActivityLogs(); }

    // View logs for one user
    public List<ActivityLog> viewUserLogs(Integer userId) { return service.getLogsByUserId(userId); }

    // View logs within date range
    public List<ActivityLog> viewLogsByRange(LocalDateTime start, LocalDateTime end) { return service.getLogsByDateRange(start, end); }
}