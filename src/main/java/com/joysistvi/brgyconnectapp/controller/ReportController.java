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

    public List<ResidentReportRow> getResidentSummary(String purok) {
        return reportService.getResidentSummary(purok);
    }

    public List<HouseholdReportRow> getHouseholdSummary(String purok) {
        return reportService.getHouseholdSummary(purok);
    }

    public List<ServiceRequestReportRow> getServiceRequestSummary(LocalDate startDate,
                                                                  LocalDate endDate,
                                                                  RequestStatus status) {
        return reportService.getServiceRequestSummary(startDate, endDate, status);
    }
}
