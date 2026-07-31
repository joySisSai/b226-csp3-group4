package com.joysistvi.brgyconnectapp.service;

import com.joysistvi.brgyconnectapp.model.Resident;
import com.joysistvi.brgyconnectapp.repository.ResidentRepo;
import com.joysistvi.brgyconnectapp.repository.ResidentRepoImpl;

import java.util.List;

public class ResidentService {
    private final ResidentRepo repo = new ResidentRepoImpl();

    public List<Resident> getAllResidents() { return repo.getAll(); }
    public Resident getResidentById(int id) { return repo.getById(id).orElse(null); }

    // Search handler — rejects empty search terms
    public List<Resident> searchResidents(String keyword) {
        if (keyword == null || keyword.isBlank()) return List.of();
        return repo.searchByNameOrCode(keyword.trim());
    }

    // Add resident — checks for required fields and duplicate codes
    public String addResident(Resident resident) {
        if (resident.getResidentCode().isBlank() || resident.getFirstName().isBlank() || resident.getLastName().isBlank())
            return "Required fields cannot be empty";
        if (repo.getByCode(resident.getResidentCode()).isPresent())
            return "Resident code already exists";
        return repo.save(resident) ? "Resident added successfully" : "Failed to add resident";
    }

    // Update resident — validates that the ID is valid
    public String updateResident(Resident resident) {
        if (resident.getResidentId() <= 0) return "Invalid resident ID";
        return repo.update(resident) ? "Resident updated successfully" : "Failed to update resident";
    }

    // Delete resident — validates that the ID is valid
    public String deleteResident(int id) {
        if (id <= 0) return "Invalid resident ID";
        return repo.delete(id) ? "Resident deleted successfully" : "Failed to delete resident";
    }
}