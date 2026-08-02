package com.joysistvi.brgyconnectapp.repository;

import com.joysistvi.brgyconnectapp.model.HouseholdReportRow;
import com.joysistvi.brgyconnectapp.model.RequestStatus;
import com.joysistvi.brgyconnectapp.model.ResidentReportRow;
import com.joysistvi.brgyconnectapp.model.ServiceRequestReportRow;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public interface ReportRepo {
    List<ResidentReportRow> getResidentSummary(String purok) throws SQLException;

    List<HouseholdReportRow> getHouseholdSummary(String purok) throws SQLException;

    List<ServiceRequestReportRow> getServiceRequestSummary(LocalDate startDate,
                                                           LocalDate endDate,
                                                           RequestStatus status) throws SQLException;
}
