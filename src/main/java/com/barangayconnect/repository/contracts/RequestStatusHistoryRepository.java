package com.barangayconnect.repository.contracts;

import com.barangayconnect.model.records.RequestStatusHistory;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface RequestStatusHistoryRepository {
    RequestStatusHistory save(RequestStatusHistory history) throws SQLException;
    RequestStatusHistory save(Connection connection, RequestStatusHistory history)
            throws SQLException;
    List<RequestStatusHistory> findByRequestId(long requestId) throws SQLException;
}
