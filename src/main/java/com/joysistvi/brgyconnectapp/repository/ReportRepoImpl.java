package com.joysistvi.brgyconnectapp.repository;

import com.joysistvi.brgyconnectapp.config.ConnectionFactory;
import com.joysistvi.brgyconnectapp.model.HouseholdReportRow;
import com.joysistvi.brgyconnectapp.model.RequestStatus;
import com.joysistvi.brgyconnectapp.model.ResidentReportRow;
import com.joysistvi.brgyconnectapp.model.ServiceRequestReportRow;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReportRepoImpl implements ReportRepo {
    private final ConnectionFactory connectionFactory;

    public ReportRepoImpl(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public List<ResidentReportRow> getResidentSummary(String purok) throws SQLException {
        String sql = """
                SELECT COALESCE(h.purok, 'Unassigned') AS purok,
                       COUNT(r.resident_id) AS total_residents,
                       SUM(r.residency_status = 'ACTIVE') AS active_residents,
                       SUM(r.residency_status = 'TRANSFERRED') AS transferred_residents,
                       SUM(r.residency_status = 'DECEASED') AS deceased_residents,
                       SUM(r.residency_status = 'INACTIVE') AS inactive_residents,
                       SUM(r.is_registered_voter = TRUE) AS registered_voters
                FROM residents r
                LEFT JOIN households h ON h.household_id = r.household_id
                WHERE (? IS NULL OR h.purok = ?)
                GROUP BY COALESCE(h.purok, 'Unassigned')
                ORDER BY purok
                """;
        List<ResidentReportRow> rows = new ArrayList<>();
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setOptionalString(statement, 1, purok);
            setOptionalString(statement, 2, purok);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    rows.add(new ResidentReportRow(
                            resultSet.getString("purok"),
                            resultSet.getLong("total_residents"),
                            resultSet.getLong("active_residents"),
                            resultSet.getLong("transferred_residents"),
                            resultSet.getLong("deceased_residents"),
                            resultSet.getLong("inactive_residents"),
                            resultSet.getLong("registered_voters")
                    ));
                }
            }
        }
        return rows;
    }

    @Override
    public List<HouseholdReportRow> getHouseholdSummary(String purok) throws SQLException {
        String sql = """
                SELECT h.purok,
                       COUNT(DISTINCT h.household_id) AS total_households,
                       COUNT(DISTINCT CASE WHEN h.household_status = 'ACTIVE'
                                           THEN h.household_id END) AS active_households,
                       COUNT(DISTINCT CASE WHEN h.household_status = 'INACTIVE'
                                           THEN h.household_id END) AS inactive_households,
                       COUNT(r.resident_id) AS total_members
                FROM households h
                LEFT JOIN residents r ON r.household_id = h.household_id
                WHERE (? IS NULL OR h.purok = ?)
                GROUP BY h.purok
                ORDER BY h.purok
                """;
        List<HouseholdReportRow> rows = new ArrayList<>();
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setOptionalString(statement, 1, purok);
            setOptionalString(statement, 2, purok);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    rows.add(new HouseholdReportRow(
                            resultSet.getString("purok"),
                            resultSet.getLong("total_households"),
                            resultSet.getLong("active_households"),
                            resultSet.getLong("inactive_households"),
                            resultSet.getLong("total_members")
                    ));
                }
            }
        }
        return rows;
    }

    @Override
    public List<ServiceRequestReportRow> getServiceRequestSummary(LocalDate startDate,
                                                                  LocalDate endDate,
                                                                  RequestStatus status) throws SQLException {
        String sql = """
                SELECT st.service_name,
                       COUNT(sr.request_id) AS total_requests,
                       SUM(sr.status = 'PENDING') AS pending_requests,
                       SUM(sr.status = 'UNDER_REVIEW') AS under_review_requests,
                       SUM(sr.status = 'APPROVED') AS approved_requests,
                       SUM(sr.status = 'RELEASED') AS released_requests,
                       SUM(sr.status = 'REJECTED') AS rejected_requests,
                       SUM(sr.status = 'CANCELLED') AS cancelled_requests,
                       COALESCE(SUM(sr.service_fee_snapshot), 0) AS total_fees
                FROM service_types st
                LEFT JOIN service_requests sr
                       ON sr.service_type_id = st.service_type_id
                      AND sr.request_date BETWEEN ? AND ?
                      AND (? IS NULL OR sr.status = ?)
                GROUP BY st.service_type_id, st.service_name
                ORDER BY st.service_name
                """;
        List<ServiceRequestReportRow> rows = new ArrayList<>();
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(startDate));
            statement.setDate(2, Date.valueOf(endDate));
            setOptionalStatus(statement, 3, status);
            setOptionalStatus(statement, 4, status);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    rows.add(new ServiceRequestReportRow(
                            resultSet.getString("service_name"),
                            resultSet.getLong("total_requests"),
                            resultSet.getLong("pending_requests"),
                            resultSet.getLong("under_review_requests"),
                            resultSet.getLong("approved_requests"),
                            resultSet.getLong("released_requests"),
                            resultSet.getLong("rejected_requests"),
                            resultSet.getLong("cancelled_requests"),
                            resultSet.getBigDecimal("total_fees")
                    ));
                }
            }
        }
        return rows;
    }

    private void setOptionalString(PreparedStatement statement, int index, String value) throws SQLException {
        if (value == null || value.isBlank()) {
            statement.setNull(index, Types.VARCHAR);
        } else {
            statement.setString(index, value.trim());
        }
    }

    private void setOptionalStatus(PreparedStatement statement,
                                   int index,
                                   RequestStatus status) throws SQLException {
        if (status == null) {
            statement.setNull(index, Types.VARCHAR);
        } else {
            statement.setString(index, status.name());
        }
    }
}
