package com.barangayconnect.model.records;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ServiceType(
        Integer id,
        String code,
        String name,
        String description,
        BigDecimal defaultFee,
        int expectedProcessingDays,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
