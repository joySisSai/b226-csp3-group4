package com.joysistvi.brgyconnectapp.repository;

import com.joysistvi.brgyconnectapp.config.DbConnection;
import com.joysistvi.brgyconnectapp.model.RequestStatus;
import com.joysistvi.brgyconnectapp.model.ServiceRequest;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RequestStatusRepoImpl implements RequestStatusRepo {

    @Override
    public ServiceRequest findById(Long requestId) {

        String sql = "SELECT request_id, status, remarks FROM service_requests WHERE request_id = ?";

        try (Connection conn = new DbConnection().openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, requestId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                ServiceRequest request = new ServiceRequest();

                request.setRequestId(rs.getLong("request_id"));
                request.setStatus(RequestStatus.valueOf(rs.getString("status")));
                request.setRemarks(rs.getString("remarks"));

                return request;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean updateStatus(Long requestId,
                                RequestStatus status,
                                Integer processedByUserId,
                                String remarks) {

        String sql = """
                UPDATE service_requests
                SET status = ?,
                    remarks = ?,
                    processed_by_user_id = ?,
                    processed_at = NOW()
                WHERE request_id = ?
                """;

        try (Connection conn = new DbConnection().openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            stmt.setString(2, remarks);
            stmt.setInt(3, processedByUserId);
            stmt.setLong(4, requestId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean releaseRequest(Long requestId,
                                  Integer processedByUserId,
                                  String remarks) {

        String sql = """
                UPDATE service_requests
                SET status = ?,
                    remarks = ?,
                    processed_by_user_id = ?,
                    processed_at = NOW(),
                    released_at = NOW()
                WHERE request_id = ?
                """;

        try (Connection conn = new DbConnection().openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, RequestStatus.RELEASED.name());
            stmt.setString(2, remarks);
            stmt.setInt(3, processedByUserId);
            stmt.setLong(4, requestId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}