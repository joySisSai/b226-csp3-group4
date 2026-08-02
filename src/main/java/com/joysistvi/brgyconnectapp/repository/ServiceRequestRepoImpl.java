package com.joysistvi.brgyconnectapp.repository;

import com.joysistvi.brgyconnectapp.config.ConnectionFactory;
import com.joysistvi.brgyconnectapp.model.RequestStatus;
import com.joysistvi.brgyconnectapp.model.RequestStatusHistory;
import com.joysistvi.brgyconnectapp.model.ServiceRequest;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ServiceRequestRepoImpl implements ServiceRequestRepo {
    private static final String REQUEST_COLUMNS = """
            sr.request_id, sr.request_number, sr.resident_id, sr.service_type_id,
            sr.purpose, sr.request_date, sr.service_fee_snapshot, sr.status,
            sr.remarks, sr.created_by_user_id, sr.processed_by_user_id,
            sr.processed_at, sr.released_at, sr.created_at, sr.updated_at
            """;

    private final ConnectionFactory connectionFactory;

    public ServiceRequestRepoImpl(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public List<ServiceRequest> getRecent(int offset, int limit) throws SQLException {
        String sql = "SELECT " + REQUEST_COLUMNS + """
                FROM service_requests sr
                ORDER BY sr.created_at DESC
                LIMIT ? OFFSET ?
                """;
        List<ServiceRequest> requests = new ArrayList<>();
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            statement.setInt(2, offset);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    requests.add(mapRequest(resultSet));
                }
            }
        }
        return requests;
    }

    @Override
    public List<ServiceRequest> getOwn(int residentId, int offset, int limit) throws SQLException {
        String sql = "SELECT " + REQUEST_COLUMNS + """
                FROM service_requests sr
                WHERE sr.resident_id = ?
                ORDER BY sr.created_at DESC
                LIMIT ? OFFSET ?
                """;
        List<ServiceRequest> requests = new ArrayList<>();
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, residentId);
            statement.setInt(2, limit);
            statement.setInt(3, offset);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    requests.add(mapRequest(resultSet));
                }
            }
        }
        return requests;
    }

    @Override
    public List<ServiceRequest> search(String keyword, int offset, int limit) throws SQLException {
        String sql = "SELECT " + REQUEST_COLUMNS + """
                FROM service_requests sr
                JOIN residents r ON r.resident_id = sr.resident_id
                WHERE sr.request_number LIKE ?
                   OR r.resident_code LIKE ?
                   OR r.first_name LIKE ?
                   OR r.last_name LIKE ?
                ORDER BY sr.created_at DESC
                LIMIT ? OFFSET ?
                """;
        List<ServiceRequest> requests = new ArrayList<>();
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            statement.setString(1, pattern);
            statement.setString(2, pattern);
            statement.setString(3, pattern);
            statement.setString(4, pattern);
            statement.setInt(5, limit);
            statement.setInt(6, offset);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    requests.add(mapRequest(resultSet));
                }
            }
        }
        return requests;
    }

    @Override
    public Optional<ServiceRequest> getById(long requestId) throws SQLException {
        String sql = "SELECT " + REQUEST_COLUMNS + " FROM service_requests sr WHERE sr.request_id = ?";
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, requestId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRequest(resultSet)) : Optional.empty();
            }
        }
    }

    @Override
    public Optional<ServiceRequest> getByNumber(String requestNumber) throws SQLException {
        String sql = "SELECT " + REQUEST_COLUMNS + " FROM service_requests sr WHERE sr.request_number = ?";
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, requestNumber);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRequest(resultSet)) : Optional.empty();
            }
        }
    }

    @Override
    public long saveWithInitialHistory(ServiceRequest request) throws SQLException {
        String insertRequestSql = """
                INSERT INTO service_requests (
                    request_number, resident_id, service_type_id, purpose, request_date,
                    service_fee_snapshot, status, remarks, created_by_user_id
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        String insertHistorySql = """
                INSERT INTO request_status_history (
                    request_id, old_status, new_status, remarks, changed_by_user_id
                ) VALUES (?, NULL, ?, ?, ?)
                """;

        try (Connection connection = connectionFactory.openConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement requestStatement = connection.prepareStatement(
                    insertRequestSql, Statement.RETURN_GENERATED_KEYS)) {
                requestStatement.setString(1, request.getRequestNumber());
                requestStatement.setInt(2, request.getResidentId());
                requestStatement.setInt(3, request.getServiceTypeId());
                requestStatement.setString(4, request.getPurpose());
                requestStatement.setDate(5, Date.valueOf(request.getRequestDate()));
                requestStatement.setBigDecimal(6, request.getServiceFeeSnapshot());
                requestStatement.setString(7, request.getStatus().name());
                setNullableString(requestStatement, 8, request.getRemarks());
                requestStatement.setInt(9, request.getCreatedByUserId());
                requestStatement.executeUpdate();

                long requestId;
                try (ResultSet keys = requestStatement.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("No request ID was generated");
                    }
                    requestId = keys.getLong(1);
                }

                try (PreparedStatement historyStatement = connection.prepareStatement(insertHistorySql)) {
                    historyStatement.setLong(1, requestId);
                    historyStatement.setString(2, request.getStatus().name());
                    setNullableString(historyStatement, 3, request.getRemarks());
                    historyStatement.setInt(4, request.getCreatedByUserId());
                    historyStatement.executeUpdate();
                }

                connection.commit();
                return requestId;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    @Override
    public boolean updateStatus(long requestId,
                                RequestStatus expectedOldStatus,
                                RequestStatus newStatus,
                                String remarks,
                                int changedByUserId) throws SQLException {
        String updateSql = """
                UPDATE service_requests
                SET status = ?,
                    remarks = ?,
                    processed_by_user_id = ?,
                    processed_at = CASE
                        WHEN ? IN ('UNDER_REVIEW', 'APPROVED', 'REJECTED', 'CANCELLED')
                        THEN COALESCE(processed_at, NOW())
                        ELSE processed_at
                    END,
                    released_at = CASE WHEN ? = 'RELEASED' THEN NOW() ELSE released_at END,
                    updated_at = NOW()
                WHERE request_id = ? AND status = ?
                """;
        String historySql = """
                INSERT INTO request_status_history (
                    request_id, old_status, new_status, remarks, changed_by_user_id
                ) VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = connectionFactory.openConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
                updateStatement.setString(1, newStatus.name());
                setNullableString(updateStatement, 2, remarks);
                updateStatement.setInt(3, changedByUserId);
                updateStatement.setString(4, newStatus.name());
                updateStatement.setString(5, newStatus.name());
                updateStatement.setLong(6, requestId);
                updateStatement.setString(7, expectedOldStatus.name());
                if (updateStatement.executeUpdate() == 0) {
                    connection.rollback();
                    return false;
                }

                try (PreparedStatement historyStatement = connection.prepareStatement(historySql)) {
                    historyStatement.setLong(1, requestId);
                    historyStatement.setString(2, expectedOldStatus.name());
                    historyStatement.setString(3, newStatus.name());
                    setNullableString(historyStatement, 4, remarks);
                    historyStatement.setInt(5, changedByUserId);
                    historyStatement.executeUpdate();
                }

                connection.commit();
                return true;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    @Override
    public List<RequestStatusHistory> getStatusHistory(long requestId) throws SQLException {
        String sql = """
                SELECT history_id, request_id, old_status, new_status,
                       remarks, changed_by_user_id, changed_at
                FROM request_status_history
                WHERE request_id = ?
                ORDER BY changed_at, history_id
                """;
        List<RequestStatusHistory> history = new ArrayList<>();
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, requestId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    history.add(mapHistory(resultSet));
                }
            }
        }
        return history;
    }

    private ServiceRequest mapRequest(ResultSet resultSet) throws SQLException {
        ServiceRequest request = new ServiceRequest();
        request.setRequestId(resultSet.getLong("request_id"));
        request.setRequestNumber(resultSet.getString("request_number"));
        request.setResidentId(resultSet.getInt("resident_id"));
        request.setServiceTypeId(resultSet.getInt("service_type_id"));
        request.setPurpose(resultSet.getString("purpose"));
        Date requestDate = resultSet.getDate("request_date");
        request.setRequestDate(requestDate == null ? null : requestDate.toLocalDate());
        request.setServiceFeeSnapshot(resultSet.getBigDecimal("service_fee_snapshot"));
        request.setStatus(RequestStatus.valueOf(resultSet.getString("status")));
        request.setRemarks(resultSet.getString("remarks"));
        request.setCreatedByUserId(resultSet.getInt("created_by_user_id"));
        request.setProcessedByUserId(resultSet.getObject("processed_by_user_id", Integer.class));
        request.setProcessedAt(toLocalDateTime(resultSet.getTimestamp("processed_at")));
        request.setReleasedAt(toLocalDateTime(resultSet.getTimestamp("released_at")));
        request.setCreatedAt(toLocalDateTime(resultSet.getTimestamp("created_at")));
        request.setUpdatedAt(toLocalDateTime(resultSet.getTimestamp("updated_at")));
        return request;
    }

    private RequestStatusHistory mapHistory(ResultSet resultSet) throws SQLException {
        RequestStatusHistory history = new RequestStatusHistory();
        history.setHistoryId(resultSet.getLong("history_id"));
        history.setRequestId(resultSet.getLong("request_id"));
        String oldStatus = resultSet.getString("old_status");
        history.setOldStatus(oldStatus == null ? null : RequestStatus.valueOf(oldStatus));
        history.setNewStatus(RequestStatus.valueOf(resultSet.getString("new_status")));
        history.setRemarks(resultSet.getString("remarks"));
        history.setChangedByUserId(resultSet.getInt("changed_by_user_id"));
        history.setChangedAt(toLocalDateTime(resultSet.getTimestamp("changed_at")));
        return history;
    }

    private void setNullableString(PreparedStatement statement, int index, String value) throws SQLException {
        if (value == null || value.isBlank()) {
            statement.setNull(index, Types.VARCHAR);
        } else {
            statement.setString(index, value.trim());
        }
    }

    private java.time.LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
