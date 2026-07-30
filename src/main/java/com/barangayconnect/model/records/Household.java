package com.barangayconnect.model.records;

import com.barangayconnect.model.enums.HouseholdStatus;
import java.time.LocalDateTime;

public record Household(
        Integer id,
        String code,
        String addressLine,
        String purok,
        HouseholdStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
