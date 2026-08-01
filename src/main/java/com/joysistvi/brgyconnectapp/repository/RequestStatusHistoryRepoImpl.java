package com.joysistvi.brgyconnectapp.repository;

import com.joysistvi.brgyconnectapp.config.ConnectionFactory;
import com.joysistvi.brgyconnectapp.model.RequestStatus;
import com.joysistvi.brgyconnectapp.model.RequestStatusHistory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RequestStatusHistoryRepoImpl implements RequestStatusHistoryRepo {

    private final ConnectionFactory connectionFactory;

    public RequestStatusHistoryRepoImpl(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public boolean save(RequestStatusHistory history) {

        String sql = """
                INSERT INTO request_status_history
                (
                    request_id,
                    old_status,
                    new_status,
                    remarks,
                    changed_by_user_id,
                    changed_at
                )
                VALUES (?, ?, ?, ?, ?, NOW())
                """;

        try (Connection conn = connectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, history.getRequestId());
            stmt.setString(2, history.getOldStatus().name());
            stmt.setString(3, history.getNewStatus().name());
            stmt.setString(4, history.getRemarks());
            stmt.setInt(5, history.getChangedByUserId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public List<RequestStatusHistory> findByRequestId(Long requestId) {

        List<RequestStatusHistory> histories = new ArrayList<>();

        String sql = """
                SELECT *
                FROM request_status_history
                WHERE request_id = ?
                ORDER BY changed_at ASC
                """;

        try (Connection conn = connectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, requestId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                RequestStatusHistory history = new RequestStatusHistory();

                history.setHistoryId(rs.getLong("history_id"));
                history.setRequestId(rs.getLong("request_id"));
                history.setOldStatus(RequestStatus.valueOf(rs.getString("old_status")));
                history.setNewStatus(RequestStatus.valueOf(rs.getString("new_status")));
                history.setRemarks(rs.getString("remarks"));
                history.setChangedByUserId(rs.getInt("changed_by_user_id"));
                history.setChangedAt(
                        rs.getTimestamp("changed_at").toLocalDateTime()
                );

                histories.add(history);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return histories;
    }
}