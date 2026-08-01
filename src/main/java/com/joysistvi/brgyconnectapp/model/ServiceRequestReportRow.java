package com.joysistvi.brgyconnectapp.model;

import java.math.BigDecimal;

public record ServiceRequestReportRow(
        String serviceName,
        long totalRequests,
        long pendingRequests,
        long underReviewRequests,
        long approvedRequests,
        long releasedRequests,
        long rejectedRequests,
        long cancelledRequests,
        BigDecimal totalFees
) {
}
