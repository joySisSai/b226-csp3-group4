package com.joysistvi.brgyconnectapp.repository;

import com.joysistvi.brgyconnectapp.model.Resident;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Repository contract for Resident data operations.
 * Follows the project's interface + implementation pattern.
 */
public interface ResidentRepo {
        List<Resident> getAll() throws SQLException;
        List<Resident> getAllActive() throws SQLException;
        Optional<Resident> getById(int id) throws SQLException;
        Optional<Resident> getByCode(String code) throws SQLException;
        List<Resident> searchByNameOrCode(String keyword) throws SQLException;
        boolean save(Resident resident) throws SQLException;
        boolean update(Resident resident) throws SQLException;
        boolean deactivate(int id) throws SQLException; // soft delete
    }
