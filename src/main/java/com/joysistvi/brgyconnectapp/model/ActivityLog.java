package com.joysistvi.brgyconnectapp.model;

import java.time.LocalDateTime;

public class ActivityLog {
    private Long activityLogId;
    private Integer userId;
    private String action;
    private String entityType;
    private Long entityId;
    private String description;
    private LocalDateTime createdAt;

    public ActivityLog() {
    }

    public ActivityLog(Long activityLogId, Integer userId, String action, String entityType,
                       Long entityId, String description, LocalDateTime createdAt) {
        this.activityLogId = activityLogId;
        this.userId = userId;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.description = description;
        this.createdAt = createdAt;
    }

    public Long getActivityLogId() { return activityLogId; }
    public void setActivityLogId(Long activityLogId) { this.activityLogId = activityLogId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
