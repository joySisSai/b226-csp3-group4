package com.barangayconnect.repository.contracts;

import com.barangayconnect.model.records.Household;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface HouseholdRepository {
    Household save(Household household) throws SQLException;
    Optional<Household> findById(int id) throws SQLException;
    Optional<Household> findByCode(String code) throws SQLException;
    List<Household> findAll() throws SQLException;
}
