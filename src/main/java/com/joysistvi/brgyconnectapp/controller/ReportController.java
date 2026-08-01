package com.joysistvi.brgyconnectapp.controller;

import com.joysistvi.brgyconnectapp.model.HouseholdReportRow;
import com.joysistvi.brgyconnectapp.model.RequestStatus;
import com.joysistvi.brgyconnectapp.model.ResidentReportRow;
import com.joysistvi.brgyconnectapp.model.ServiceRequestReportRow;
import com.joysistvi.brgyconnectapp.service.ReportService;

import java.time.LocalDate;
import java.util.List;

public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    public List<ResidentReportRow> getResidentSummary(String purok, int actingUserId) {
        return reportService.getResidentSummary(purok, actingUserId);
    }

    public List<HouseholdReportRow> getHouseholdSummary(String purok, int actingUserId) {
        return reportService.getHouseholdSummary(purok, actingUserId);
    }

    public List<ServiceRequestReportRow> getServiceRequestSummary(LocalDate startDate,
                                                                  LocalDate endDate,
                                                                  RequestStatus status,
                                                                  int actingUserId) {
        return reportService.getServiceRequestSummary(startDate, endDate, status, actingUserId);
    }
}
