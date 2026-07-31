package com.joysistvi.brgyconnectapp.service;

import com.joysistvi.brgyconnectapp.model.Resident;
import com.joysistvi.brgyconnectapp.repository.ResidentRepo;
import com.joysistvi.brgyconnectapp.repository.ResidentRepoImpl;

import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Service layer for Resident business logic and validation.
 */
public class ResidentService {

    private final ResidentRepo residentRepo = new ResidentRepoImpl();

    private static final Pattern PHONE_PATTERN = Pattern.compile("^09\\d{9}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public List<Resident> getAllResidents() {
        return residentRepo.getAllResidents();
    }

    public Resident getResidentById(int id) {
        if (id <= 0) return null;
        return residentRepo.getById(id);
    }

    /**
     * Validates input before saving to database.
     * Uses Enum values instead of plain strings for status fields.
     */
    public String addResident(Resident r) {
        if (r.getFirstName() == null || r.getFirstName().isBlank() ||
                r.getLastName() == null || r.getLastName().isBlank() ||
                r.getResidentCode() == null || r.getResidentCode().isBlank() ||
                r.getBirthDate() == null || r.getSex() == null || r.getCivilStatus() == null) {
            return "Missing required fields";
        }

        if (r.getContactNumber() != null && !r.getContactNumber().isBlank()) {
            if (!PHONE_PATTERN.matcher(r.getContactNumber()).matches()) {
                return "Invalid contact number format";
            }
        }

        if (r.getEmail() != null && !r.getEmail().isBlank()) {
            if (!EMAIL_PATTERN.matcher(r.getEmail()).matches()) {
                return "Invalid email format";
            }
        }

        if (r.getBirthDate().isAfter(LocalDate.now())) {
            return "Birth date cannot be in the future";
        }

        if (r.getDateRegistered() == null) {
            r.setDateRegistered(LocalDate.now());
        }

        return residentRepo.create(r) ? "Resident added successfully" : "Failed to add resident";
    }

    public String updateResident(Resident r) {
        if (r.getResidentId() <= 0) return "Invalid resident ID";
        String validation = addResident(r);
        if (!validation.equals("Resident added successfully")) return validation;
        return residentRepo.update(r) ? "Resident updated successfully" : "Failed to update resident";
    }

    public String deleteResident(int id) {
        if (id <= 0) return "Invalid resident ID";
        return residentRepo.delete(id) ? "Resident deleted successfully" : "Failed to delete resident";
    }
}