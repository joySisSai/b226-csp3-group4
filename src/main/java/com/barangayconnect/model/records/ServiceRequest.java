package com.barangayconnect.model.records;

import com.barangayconnect.model.enums.RequestStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ServiceRequest(
        Long id,
        String requestNumber,
        Integer residentId,
        Integer serviceTypeId,
        String purpose,
        LocalDate requestDate,
        BigDecimal serviceFeeSnapshot,
        RequestStatus status,
        String remarks,
        Integer createdByUserId,
        Integer processedByUserId,
        LocalDateTime processedAt,
        LocalDateTime releasedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
