package com.barangayconnect.model.records;

import com.barangayconnect.model.enums.RequestStatus;
import java.time.LocalDateTime;

public record RequestStatusHistory(
        Long id,
        Long requestId,
        RequestStatus oldStatus,
        RequestStatus newStatus,
        String remarks,
        Integer changedByUserId,
        LocalDateTime changedAt) {
}
