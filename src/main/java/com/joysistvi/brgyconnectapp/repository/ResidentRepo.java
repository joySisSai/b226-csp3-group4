package com.joysistvi.brgyconnectapp.repository;

import com.joysistvi.brgyconnectapp.model.Resident;
import java.util.List;
import java.util.Optional;

/**
 * Repository contract for Resident data operations.
 * Follows the project's interface + implementation pattern.
 */
public interface ResidentRepo {
        List<Resident> getAll();
        List<Resident> getAllActive();
        Optional<Resident> getById(int id);
        Optional<Resident> getByCode(String code);
        List<Resident> searchByNameOrCode(String keyword);
        boolean save(Resident resident);
        boolean update(Resident resident);
        boolean deactivate(int id); // soft delete
    }