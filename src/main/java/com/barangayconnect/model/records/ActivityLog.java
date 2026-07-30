package com.barangayconnect.model.records;

import java.time.LocalDateTime;

public record ActivityLog(
        Long id,
        Integer userId,
        String action,
        String entityType,
        Long entityId,
        String description,
        LocalDateTime createdAt) {
}
