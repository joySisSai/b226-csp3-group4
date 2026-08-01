package com.joysistvi.brgyconnectapp.service;

import com.joysistvi.brgyconnectapp.model.HouseholdReportRow;
import com.joysistvi.brgyconnectapp.model.RequestStatus;
import com.joysistvi.brgyconnectapp.model.ResidentReportRow;
import com.joysistvi.brgyconnectapp.model.ServiceRequestReportRow;
import com.joysistvi.brgyconnectapp.repository.ReportRepo;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ReportService {
    private final ReportRepo reportRepo;
    private final AuthorizationService authorizationService;

    public ReportService(ReportRepo reportRepo, AuthorizationService authorizationService) {
        this.reportRepo = reportRepo;
        this.authorizationService = authorizationService;
    }

    public List<ResidentReportRow> getResidentSummary(String purok, int actingUserId) {
        if (!canGenerate(actingUserId)) {
            return List.of();
        }
        try {
            return reportRepo.getResidentSummary(normalizeFilter(purok));
        } catch (SQLException exception) {
            return List.of();
        }
    }

    public List<HouseholdReportRow> getHouseholdSummary(String purok, int actingUserId) {
        if (!canGenerate(actingUserId)) {
            return List.of();
        }
        try {
            return reportRepo.getHouseholdSummary(normalizeFilter(purok));
        } catch (SQLException exception) {
            return List.of();
        }
    }

    public List<ServiceRequestReportRow> getServiceRequestSummary(LocalDate startDate,
                                                                  LocalDate endDate,
                                                                  RequestStatus status,
                                                                  int actingUserId) {
        if (!canGenerate(actingUserId)) {
            return List.of();
        }
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return List.of();
        }
        try {
            return reportRepo.getServiceRequestSummary(startDate, endDate, status);
        } catch (SQLException exception) {
            return List.of();
        }
    }

    private String normalizeFilter(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public boolean canGenerate(int actingUserId) {
        return authorizationService.canAccessStaffOperations(actingUserId);
    }
}
