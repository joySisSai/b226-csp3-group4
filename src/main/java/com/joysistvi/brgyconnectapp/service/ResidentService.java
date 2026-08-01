package com.joysistvi.brgyconnectapp.service;

import com.joysistvi.brgyconnectapp.model.Resident;
import com.joysistvi.brgyconnectapp.repository.ResidentRepo;
import com.joysistvi.brgyconnectapp.validation.ResidentFieldValidator;

import java.sql.SQLException;
import java.util.List;

public class ResidentService {
    private static final String DATABASE_ERROR =
            "Unable to complete the operation because the database is unavailable";
    private final ResidentRepo repo;
    private final AuthorizationService authorizationService;

    public ResidentService(ResidentRepo repo, AuthorizationService authorizationService) {
        this.repo = repo;
        this.authorizationService = authorizationService;
    }

    public List<Resident> getAllResidents(int actingUserId) {
        if (!canManage(actingUserId)) {
            return List.of();
        }
        try {
            return repo.getAll();
        } catch (SQLException exception) {
            throw new DataAccessException(exception);
        }
    }

    public Resident getResidentById(int id, int actingUserId) {
        if (!canManage(actingUserId)) {
            return null;
        }
        try {
            return repo.getById(id).orElse(null);
        } catch (SQLException exception) {
            throw new DataAccessException(exception);
        }
    }

    public Resident getOwnResidentProfile(int residentId, int actingUserId) {
        if (authorizationService == null ||
                !authorizationService.canViewOwnResidentProfile(actingUserId, residentId)) {
            return null;
        }
        try {
            return repo.getById(residentId).orElse(null);
        } catch (SQLException exception) {
            throw new DataAccessException(exception);
        }
    }

    public List<Resident> getAllActive(int actingUserId) {
        if (!canManage(actingUserId)) {
            return List.of();
        }
        try {
            return repo.getAllActive();
        } catch (SQLException exception) {
            throw new DataAccessException(exception);
        }
    }

    // Add a resident after validation; the repository assigns the resident code.
    public String addResident(Resident resident, int actingUserId) {
        if (!canManage(actingUserId)) {
            return AuthorizationService.STAFF_ACCESS_DENIED;
        }
        String validationError = validateResident(resident);
        if (validationError != null) {
            return validationError;
        }

        try {
            if (!repo.save(resident)) {
                return "Failed to add resident";
            }
            return "Resident added successfully. Resident code: " + resident.getResidentCode();
        } catch (SQLException exception) {
            return DatabaseErrors.isConstraintViolation(exception)
                    ? "Resident information conflicts with an existing record"
                    : DATABASE_ERROR;
        }
    }

    public List<Resident> searchResidents(String keyword, int actingUserId) {
        if (!canManage(actingUserId)) {
            return List.of();
        }
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }

        try {
            return repo.searchByNameOrCode(keyword.trim());
        } catch (SQLException exception) {
            throw new DataAccessException(exception);
        }
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
        try {
            return repo.update(resident)
                    ? "Resident updated successfully"
                    : "Failed to update resident";
        } catch (SQLException exception) {
            return DatabaseErrors.isConstraintViolation(exception)
                    ? "Resident information conflicts with an existing record"
                    : DATABASE_ERROR;
        }
    }
    // Deactivate resident - soft delete
    public String deactivateResident(int id, int actingUserId) {
        if (!canManage(actingUserId)) {
            return AuthorizationService.STAFF_ACCESS_DENIED;
        }
        if (id <= 0) return "Invalid resident ID";
        try {
            return repo.deactivate(id)
                    ? "Resident marked as inactive"
                    : "Failed to update status";
        } catch (SQLException exception) {
            return DATABASE_ERROR;
        }
    }

    private String validateResident(Resident resident) {
        if (resident == null) {
            return "Resident information is required";
        }
        if (isBlank(resident.getFirstName()) ||
                isBlank(resident.getLastName())) {
            return "First name and last name are required";
        }
        if (resident.getSex() == null ||
                resident.getCivilStatus() == null ||
                resident.getResidencyStatus() == null) {
            return "Sex, civil status, and residency status are required";
        }
        String birthDateError = ResidentFieldValidator.validateBirthDate(resident.getBirthDate());
        if (birthDateError != null) {
            return birthDateError;
        }

        resident.setContactNumber(ResidentFieldValidator.normalizeOptional(resident.getContactNumber()));
        String contactNumberError = ResidentFieldValidator.validateContactNumber(resident.getContactNumber());
        if (contactNumberError != null) {
            return contactNumberError;
        }

        resident.setEmail(ResidentFieldValidator.normalizeOptional(resident.getEmail()));
        String emailError = ResidentFieldValidator.validateEmail(resident.getEmail());
        if (emailError != null) {
            return emailError;
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
