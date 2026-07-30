package com.barangayconnect.repository.contracts;

import com.barangayconnect.model.records.Resident;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ResidentRepository {
    Resident save(Resident resident) throws SQLException;
    Optional<Resident> findById(int id) throws SQLException;
    Optional<Resident> findByCode(String code) throws SQLException;
    List<Resident> findByHouseholdId(int householdId) throws SQLException;
}
