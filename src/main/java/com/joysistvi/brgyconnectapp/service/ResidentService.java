package com.joysistvi.brgyconnectapp.service;

import com.joysistvi.brgyconnectapp.model.Resident;
import com.joysistvi.brgyconnectapp.repository.ResidentRepo;
import com.joysistvi.brgyconnectapp.repository.ResidentRepoImpl;

import java.util.List;

public class ResidentService {
    private final ResidentRepo repo = new ResidentRepoImpl();

    public List<Resident> getAllResidents() { return repo.getAll(); }
    public Resident getResidentById(int id) { return repo.getById(id).orElse(null); }
    public List<Resident> getAllActive() { return repo.getAllActive();}

    // Add resident — checks for required fields and duplicate codes
    public String addResident(Resident resident) {
        if (resident.getResidentCode().isBlank() || resident.getFirstName().isBlank()
                || resident.getLastName().isBlank() || resident.getContactNumber().isBlank())
            return "Resident Code, Name, and Contact are required";
        if (resident.getBirthDate() == null)
            return "Birth date is required";
        if (repo.getByCode(resident.getResidentCode()).isPresent())
            return "Resident code already exists";
        return repo.save(resident) ? "Resident added successfully" : "Failed to add resident";
    }

    // Update resident — validates that the ID is valid
    public String updateResident(Resident resident) {
        if (resident.getResidentId() <= 0) return "Invalid resident ID";
        return repo.update(resident) ? "Resident updated successfully" : "Failed to update resident";
    }
    // Deactivate resident - soft delete
    public String deactivateResident(int id) {
        if (id <= 0) return "Invalid resident ID";
        return repo.deactivate(id) ? "Resident marked as inactive" : "Failed to update status";
    }
}