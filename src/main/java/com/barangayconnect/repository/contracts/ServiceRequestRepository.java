package com.barangayconnect.repository.contracts;

import com.barangayconnect.model.enums.RequestStatus;
import com.barangayconnect.model.records.ServiceRequest;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ServiceRequestRepository {
    ServiceRequest save(ServiceRequest request) throws SQLException;
    ServiceRequest save(Connection connection, ServiceRequest request) throws SQLException;
    Optional<ServiceRequest> findById(long id) throws SQLException;
    Optional<ServiceRequest> findByRequestNumber(String requestNumber) throws SQLException;
    List<ServiceRequest> findByResidentId(int residentId) throws SQLException;
    void updateStatus(Connection connection, long id, RequestStatus status, String remarks)
            throws SQLException;
}
