package com.joysistvi.brgyconnectapp.repository;

import com.joysistvi.brgyconnectapp.model.RequestStatus;
import com.joysistvi.brgyconnectapp.model.RequestStatusHistory;
import com.joysistvi.brgyconnectapp.model.ServiceRequest;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ServiceRequestRepo {
    List<ServiceRequest> getRecent(int maximumRows) throws SQLException;

    List<ServiceRequest> search(String keyword, int maximumRows) throws SQLException;

    Optional<ServiceRequest> getById(long requestId) throws SQLException;

    Optional<ServiceRequest> getByNumber(String requestNumber) throws SQLException;

    long saveWithInitialHistory(ServiceRequest request) throws SQLException;

    boolean updateStatus(long requestId,
                         RequestStatus expectedOldStatus,
                         RequestStatus newStatus,
                         String remarks,
                         int changedByUserId) throws SQLException;

    List<RequestStatusHistory> getStatusHistory(long requestId) throws SQLException;
}
