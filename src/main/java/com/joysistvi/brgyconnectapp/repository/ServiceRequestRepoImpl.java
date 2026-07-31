package com.joysistvi.brgyconnectapp.repository;

import com.joysistvi.brgyconnectapp.config.ConnectionFactory;
import com.joysistvi.brgyconnectapp.config.DbConnection;
import com.joysistvi.brgyconnectapp.model.RequestStatus;
import com.joysistvi.brgyconnectapp.model.ServiceRequest;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ServiceRequestRepoImpl implements ServiceRequestRepo {
    private final ConnectionFactory dbFactory = new DbConnection();

    // Generate unique request number: SR-YYYYMMDD-SEQUENCE
    private String generateRequestNumber() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int sequence = 1;
        try (Connection conn = dbFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT COUNT(*) FROM service_requests WHERE request_number LIKE ?")) {
            stmt.setString(1, "SR-" + datePart + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) sequence = rs.getInt(1) + 1;
            }
        } catch (SQLException e) {
            System.err.println("Error generating request number: " + e.getMessage());
        }
        return String.format("SR-%s-%04d", datePart, sequence);
    }

    // Check if same resident already has pending request for same service type today
    private boolean isDuplicateRequest(Integer residentId, Integer serviceTypeId) {
        try (Connection conn = dbFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT COUNT(*) FROM service_requests WHERE resident_id = ? AND service_type_id = ? AND request_date = ? AND status = 'PENDING'")) {
            stmt.setInt(1, residentId);
            stmt.setInt(2, serviceTypeId);
            stmt.setDate(3, Date.valueOf(LocalDate.now()));
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Duplicate check error: " + e.getMessage());
        }
        return false;
    }

    private static final String SELECT_ALL = "SELECT * FROM service_requests ORDER BY created_at DESC";
    private static final String SELECT_BY_RESIDENT = "SELECT * FROM service_requests WHERE resident_id = ? ORDER BY created_at DESC";
    private static final String SELECT_BY_ID = "SELECT * FROM service_requests WHERE request_id = ?";
    private static final String INSERT = """
        INSERT INTO service_requests (
            request_number, resident_id, service_type_id, purpose, request_date,
            service_fee_snapshot, status, remarks, created_by_user_id
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
    private static final String UPDATE = """
        UPDATE service_requests SET
            service_type_id = ?, purpose = ?, request_date = ?, service_fee_snapshot = ?,
            status = ?, remarks = ?, processed_by_user_id = ?, processed_at = ?,
            released_at = ?, updated_at = NOW()
        WHERE request_id = ?
        """;

    // Get all service requests
    @Override
    public List<ServiceRequest> getAllRequests() {
        List<ServiceRequest> list = new ArrayList<>();
        try (Connection conn = dbFactory.openConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL)) {
            while (rs.next()) list.add(mapToRequest(rs));
        } catch (SQLException e) {
            System.err.println("Fetch all requests error: " + e.getMessage());
        }
        return list;
    }

    // Get all requests made by a specific resident
    @Override
    public List<ServiceRequest> getByResidentId(Integer residentId) {
        List<ServiceRequest> list = new ArrayList<>();
        try (Connection conn = dbFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_RESIDENT)) {
            stmt.setInt(1, residentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapToRequest(rs));
            }
        } catch (SQLException e) {
            System.err.println("Fetch resident requests error: " + e.getMessage());
        }
        return list;
    }

    // Get single request by ID
    @Override
    public ServiceRequest getById(Long id) {
        try (Connection conn = dbFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapToRequest(rs) : null;
            }
        } catch (SQLException e) {
            System.err.println("Fetch request by ID error: " + e.getMessage());
        }
        return null;
    }

    // Create new request — auto-generate number, validate no duplicate, set default status
    @Override
    public boolean create(ServiceRequest request) {
        // Prevent duplicate pending requests
        if (isDuplicateRequest(request.getResidentId(), request.getServiceTypeId())) {
            System.err.println("Duplicate request detected for this resident and service type");
            return false;
        }
        // Auto-generate unique request number
        request.setRequestNumber(generateRequestNumber());
        // Set default status if not provided
        if (request.getStatus() == null) request.setStatus(RequestStatus.PENDING);

        try (Connection conn = dbFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT)) {
            stmt.setString(1, request.getRequestNumber());
            stmt.setObject(2, request.getResidentId());
            stmt.setObject(3, request.getServiceTypeId());
            stmt.setString(4, request.getPurpose());
            stmt.setDate(5, Date.valueOf(request.getRequestDate()));
            stmt.setBigDecimal(6, request.getServiceFeeSnapshot());
            stmt.setString(7, request.getStatus().name());
            stmt.setString(8, request.getRemarks());
            stmt.setObject(9, request.getCreatedByUserId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Create request error: " + e.getMessage());
        }
        return false;
    }

    // Update existing request details and status
    @Override
    public boolean update(ServiceRequest request) {
        try (Connection conn = dbFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE)) {
            stmt.setObject(1, request.getServiceTypeId());
            stmt.setString(2, request.getPurpose());
            stmt.setDate(3, Date.valueOf(request.getRequestDate()));
            stmt.setBigDecimal(4, request.getServiceFeeSnapshot());
            stmt.setString(5, request.getStatus().name());
            stmt.setString(6, request.getRemarks());
            stmt.setObject(7, request.getProcessedByUserId());
            stmt.setTimestamp(8, request.getProcessedAt() != null ? Timestamp.valueOf(request.getProcessedAt()) : null);
            stmt.setTimestamp(9, request.getReleasedAt() != null ? Timestamp.valueOf(request.getReleasedAt()) : null);
            stmt.setLong(10, request.getRequestId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Update request error: " + e.getMessage());
        }
        return false;
    }

    // Convert database row to ServiceRequest object
    private ServiceRequest mapToRequest(ResultSet rs) throws SQLException {
        ServiceRequest r = new ServiceRequest();
        r.setRequestId(rs.getLong("request_id"));
        r.setRequestNumber(rs.getString("request_number"));
        r.setResidentId(rs.getObject("resident_id", Integer.class));
        r.setServiceTypeId(rs.getObject("service_type_id", Integer.class));
        r.setPurpose(rs.getString("purpose"));
        r.setRequestDate(rs.getDate("request_date").toLocalDate());
        r.setServiceFeeSnapshot(rs.getBigDecimal("service_fee_snapshot"));
        r.setStatus(RequestStatus.valueOf(rs.getString("status")));
        r.setRemarks(rs.getString("remarks"));
        r.setCreatedByUserId(rs.getObject("created_by_user_id", Integer.class));
        r.setProcessedByUserId(rs.getObject("processed_by_user_id", Integer.class));

        Timestamp processedAt = rs.getTimestamp("processed_at");
        if (processedAt != null) r.setProcessedAt(processedAt.toLocalDateTime());

        Timestamp releasedAt = rs.getTimestamp("released_at");
        if (releasedAt != null) r.setReleasedAt(releasedAt.toLocalDateTime());

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) r.setCreatedAt(createdAt.toLocalDateTime());

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) r.setUpdatedAt(updatedAt.toLocalDateTime());

        return r;
    }
}