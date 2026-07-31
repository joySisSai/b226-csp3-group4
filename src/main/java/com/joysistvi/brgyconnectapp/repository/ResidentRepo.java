package com.joysistvi.brgyconnectapp.repository;

import com.joysistvi.brgyconnectapp.model.Resident;
import java.util.List;

/**
 * Repository contract for Resident data operations.
 * Follows the project's interface + implementation pattern.
 */
public interface ResidentRepo {
    List<Resident> getAllResidents();
    Resident getById(int id);
    boolean create(Resident resident);
    boolean update(Resident resident);
    boolean delete(int id);
}