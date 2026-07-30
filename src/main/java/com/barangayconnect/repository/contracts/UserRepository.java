package com.barangayconnect.repository.contracts;

import com.barangayconnect.model.records.User;
import java.sql.SQLException;
import java.util.Optional;

public interface UserRepository {
    User save(User user) throws SQLException;
    Optional<User> findById(int id) throws SQLException;
    Optional<User> findByUsername(String username) throws SQLException;
    Optional<User> findByResidentId(int residentId) throws SQLException;
}
