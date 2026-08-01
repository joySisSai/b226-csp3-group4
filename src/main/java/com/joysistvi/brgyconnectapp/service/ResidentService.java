package com.joysistvi.brgyconnectapp.service;

import com.joysistvi.brgyconnectapp.model.Resident;
import com.joysistvi.brgyconnectapp.repository.ResidentRepo;
import com.joysistvi.brgyconnectapp.repository.ResidentRepoImpl;

import java.time.LocalDate;
import java.util.List;

public class ResidentService {
    private final ResidentRepo repo;
    private final AuthorizationService authorizationService;

    public ResidentService() {
        this.repo = new ResidentRepoImpl();
        this.authorizationService = null;
    }

    public ResidentService(ResidentRepo repo, AuthorizationService authorizationService) {
        this.repo = repo;
        this.authorizationService = authorizationService;
    }

    public List<Resident> getAllResidents(int actingUserId) {
        return canManage(actingUserId) ? repo.getAll() : List.of();
    }

    public Resident getResidentById(int id, int actingUserId) {
        return canManage(actingUserId) ? repo.getById(id).orElse(null) : null;
    }

    public Resident getOwnResidentProfile(int residentId, int actingUserId) {
        return authorizationService != null &&
                authorizationService.canViewOwnResidentProfile(actingUserId, residentId)
                ? repo.getById(residentId).orElse(null)
                : null;
    }

    public List<Resident> getAllActive(int actingUserId) {
        return canManage(actingUserId) ? repo.getAllActive() : List.of();
    }

    // Add resident — checks for required fields and duplicate codes
    public String addResident(Resident resident, int actingUserId) {
        if (!canManage(actingUserId)) {
            return AuthorizationService.STAFF_ACCESS_DENIED;
        }
        String validationError = validateResident(resident);
        if (validationError != null) {
            return validationError;
        }

        if (repo.getByCode(resident.getResidentCode().trim()).isPresent()) {
            return "Resident code already exists";
        }

        return repo.save(resident)
                ? "Resident added successfully"
                : "Failed to add resident";
    }

    public List<Resident> searchResidents(String keyword, int actingUserId) {
        if (!canManage(actingUserId)) {
            return List.of();
        }
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }

        return repo.searchByNameOrCode(keyword.trim());
    }

    // Update resident — validates that the ID is valid
    public String updateResident(Resident resident, int actingUserId) {
        if (!canManage(actingUserId)) {
            return AuthorizationService.STAFF_ACCESS_DENIED;
        }
        if (resident == null || resident.getResidentId() == null || resident.getResidentId() <= 0) {
            return "Invalid resident ID";
        }
        String validationError = validateResident(resident);
        if (validationError != null) {
            return validationError;
        }
        return repo.update(resident) ? "Resident updated successfully" : "Failed to update resident";
    }
    // Deactivate resident - soft delete
    public String deactivateResident(int id, int actingUserId) {
        if (!canManage(actingUserId)) {
            return AuthorizationService.STAFF_ACCESS_DENIED;
        }
        if (id <= 0) return "Invalid resident ID";
        return repo.deactivate(id) ? "Resident marked as inactive" : "Failed to update status";
    }

    private String validateResident(Resident resident) {
        if (resident == null) {
            return "Resident information is required";
        }
        if (isBlank(resident.getResidentCode()) ||
                isBlank(resident.getFirstName()) ||
                isBlank(resident.getLastName())) {
            return "Resident code, first name, and last name are required";
        }
        if (resident.getBirthDate() == null ||
                resident.getSex() == null ||
                resident.getCivilStatus() == null ||
                resident.getResidencyStatus() == null) {
            return "Birth date, sex, civil status, and residency status are required";
        }
        if (resident.getBirthDate().isAfter(LocalDate.now())) {
            return "Birth date cannot be in the future";
        }
        if (resident.getHouseholdId() != null && resident.getHouseholdId() <= 0) {
            return "Household ID must be a positive whole number";
        }
        return null;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean canManage(int actingUserId) {
        return authorizationService != null &&
                authorizationService.canAccessStaffOperations(actingUserId);
    }
}
