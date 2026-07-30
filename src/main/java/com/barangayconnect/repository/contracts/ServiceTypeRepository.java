package com.barangayconnect.repository.contracts;

import com.barangayconnect.model.records.ServiceType;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ServiceTypeRepository {
    ServiceType save(ServiceType serviceType) throws SQLException;
    Optional<ServiceType> findById(int id) throws SQLException;
    Optional<ServiceType> findByCode(String code) throws SQLException;
    List<ServiceType> findActive() throws SQLException;
}
