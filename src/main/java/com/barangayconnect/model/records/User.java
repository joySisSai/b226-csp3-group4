package com.barangayconnect.model.records;

import com.barangayconnect.model.enums.AccountStatus;
import com.barangayconnect.model.enums.Role;
import java.time.LocalDateTime;

public record User(
        Integer id,
        Integer residentId,
        String username,
        String passwordHash,
        String displayName,
        Role role,
        AccountStatus status,
        int failedLoginAttempts,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
