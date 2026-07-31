package com.joysistvi.brgyconnectapp.repository;

import com.joysistvi.brgyconnectapp.model.User;

import java.sql.SQLException;
import java.util.Optional;

public interface UserRepo {
    Optional<User> findByUsername(String username) throws SQLException;

    void recordFailedLogin(int userId, int maximumAttempts) throws SQLException;

    void recordSuccessfulLogin(int userId) throws SQLException;
}
