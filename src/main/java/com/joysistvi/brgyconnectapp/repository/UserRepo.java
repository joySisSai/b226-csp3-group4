package com.joysistvi.brgyconnectapp.repository;

import com.joysistvi.brgyconnectapp.model.AccountStatus;
import com.joysistvi.brgyconnectapp.model.User;
import com.joysistvi.brgyconnectapp.model.UserRole;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface UserRepo {
    List<User> getAll() throws SQLException;

    List<User> search(String keyword, int maximumRows) throws SQLException;
    List<User> getPendingAccounts() throws SQLException;

    Optional<User> findById(int userId) throws SQLException;

    Optional<User> findByUsername(String username) throws SQLException;

    Optional<User> findByResidentId(int residentId) throws SQLException;

    boolean save(User user) throws SQLException;

    boolean updateRole(int userId, UserRole role) throws SQLException;

    boolean updateStatus(int userId, AccountStatus status) throws SQLException;

    boolean unlock(int userId) throws SQLException;

    boolean updatePassword(int userId, String passwordHash) throws SQLException;

    long countActiveAdmins() throws SQLException;

    void recordFailedLogin(int userId, int maximumAttempts) throws SQLException;

    void recordSuccessfulLogin(int userId) throws SQLException;
}
