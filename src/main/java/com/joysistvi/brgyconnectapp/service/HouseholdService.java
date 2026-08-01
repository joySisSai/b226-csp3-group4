package com.joysistvi.brgyconnectapp.service;

import com.joysistvi.brgyconnectapp.model.Household;
import com.joysistvi.brgyconnectapp.model.HouseholdStatus;
import com.joysistvi.brgyconnectapp.model.Resident;
import com.joysistvi.brgyconnectapp.model.ResidencyStatus;
import com.joysistvi.brgyconnectapp.repository.HouseholdRepo;
import com.joysistvi.brgyconnectapp.repository.ResidentRepo;

import java.sql.SQLException;
import java.util.List;

public class HouseholdService {
    private static final String DATABASE_ERROR = "Unable to complete the operation because the database is unavailable";

    private final HouseholdRepo householdRepo;
    private final ResidentRepo residentRepo;
    private final AuthorizationService authorizationService;

    public HouseholdService(HouseholdRepo householdRepo,
                            ResidentRepo residentRepo,
                            AuthorizationService authorizationService) {
        this.householdRepo = householdRepo;
        this.residentRepo = residentRepo;
        this.authorizationService = authorizationService;
    }

    public List<Household> getAllHouseholds(int actingUserId) {
        if (!canManage(actingUserId)) {
            return List.of();
        }
        try {
            return householdRepo.getAll();
        } catch (SQLException exception) {
            throw new DataAccessException(exception);
        }
    }

    public Household getHouseholdById(int householdId, int actingUserId) {
        if (!canManage(actingUserId)) {
            return null;
        }
        if (householdId <= 0) {
            return null;
        }
        try {
            return householdRepo.getById(householdId).orElse(null);
        } catch (SQLException exception) {
            throw new DataAccessException(exception);
        }
    }

    public List<Household> searchHouseholds(String keyword, int actingUserId) {
        if (!canManage(actingUserId)) {
            return List.of();
        }
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        try {
            return householdRepo.search(keyword.trim());
        } catch (SQLException exception) {
            throw new DataAccessException(exception);
        }
    }

    public List<Resident> getMembers(int householdId, int actingUserId) {
        if (!canManage(actingUserId)) {
            return List.of();
        }
        if (householdId <= 0) {
            return List.of();
        }
        try {
            return householdRepo.getMembers(householdId);
        } catch (SQLException exception) {
            throw new DataAccessException(exception);
        }
    }

    public String createHousehold(Household household, int actingUserId) {
        if (!canManage(actingUserId)) {
            return AuthorizationService.STAFF_ACCESS_DENIED;
        }
        String validationError = validateHousehold(household, false);
        if (validationError != null) {
            return validationError;
        }

        try {
            if (householdRepo.getByCode(household.getHouseholdCode().trim()).isPresent()) {
                return "Household code already exists";
            }
            household.setHouseholdCode(household.getHouseholdCode().trim());
            return householdRepo.save(household)
                    ? "Household created successfully"
                    : "Failed to create household";
        } catch (SQLException exception) {
            return DatabaseErrors.isConstraintViolation(exception)
                    ? "Household code already exists"
                    : DATABASE_ERROR;
        }
    }

    public String updateHousehold(Household household, int actingUserId) {
        if (!canManage(actingUserId)) {
            return AuthorizationService.STAFF_ACCESS_DENIED;
        }
        String validationError = validateHousehold(household, true);
        if (validationError != null) {
            return validationError;
        }

        try {
            if (householdRepo.getById(household.getHouseholdId()).isEmpty()) {
                return "Household record not found";
            }
            return householdRepo.update(household)
                    ? "Household updated successfully"
                    : "Failed to update household";
        } catch (SQLException exception) {
            return DATABASE_ERROR;
        }
    }

    public String addMember(int householdId, int residentId, int actingUserId) {
        if (!canManage(actingUserId)) {
            return AuthorizationService.STAFF_ACCESS_DENIED;
        }
        try {
            Household household = householdRepo.getById(householdId).orElse(null);
            if (household == null) {
                return "Household record not found";
            }
            if (household.getHouseholdStatus() != HouseholdStatus.ACTIVE) {
                return "Members cannot be added to an inactive household";
            }

            Resident resident = residentRepo.getById(residentId).orElse(null);
            if (resident == null) {
                return "Resident record not found";
            }
            if (resident.getResidencyStatus() != ResidencyStatus.ACTIVE) {
                return "Only active residents can be added to a household";
            }
            if (resident.getHouseholdId() != null) {
                if (resident.getHouseholdId() == householdId) {
                    return "Resident is already a member of this household";
                }
                return "Resident already belongs to another household";
            }

            return householdRepo.addMember(householdId, residentId)
                    ? "Resident added to household successfully"
                    : "Failed to add resident to household";
        } catch (SQLException exception) {
            return DATABASE_ERROR;
        }
    }

    public String removeMember(int householdId, int residentId, int actingUserId) {
        if (!canManage(actingUserId)) {
            return AuthorizationService.STAFF_ACCESS_DENIED;
        }
        try {
            if (householdRepo.getById(householdId).isEmpty()) {
                return "Household record not found";
            }
            Resident resident = residentRepo.getById(residentId).orElse(null);
            if (resident == null || resident.getHouseholdId() == null ||
                    resident.getHouseholdId() != householdId) {
                return "Resident is not a member of this household";
            }

            return householdRepo.removeMember(householdId, residentId)
                    ? "Resident removed from household successfully"
                    : "Failed to remove resident from household";
        } catch (SQLException exception) {
            return DATABASE_ERROR;
        }
    }

    public String assignHouseholdHead(int householdId, int residentId, int actingUserId) {
        if (!canManage(actingUserId)) {
            return AuthorizationService.STAFF_ACCESS_DENIED;
        }
        try {
            Household household = householdRepo.getById(householdId).orElse(null);
            if (household == null) {
                return "Household record not found";
            }
            if (household.getHouseholdStatus() != HouseholdStatus.ACTIVE) {
                return "An inactive household cannot have a household head";
            }

            Resident resident = residentRepo.getById(residentId).orElse(null);
            if (resident == null || resident.getHouseholdId() == null ||
                    resident.getHouseholdId() != householdId) {
                return "The selected resident is not a member of this household";
            }
            if (resident.getResidencyStatus() != ResidencyStatus.ACTIVE) {
                return "Only an active resident can be assigned as household head";
            }

            return householdRepo.assignHouseholdHead(householdId, residentId)
                    ? "Household head assigned successfully"
                    : "Failed to assign household head";
        } catch (SQLException exception) {
            return DATABASE_ERROR;
        }
    }

    public String deactivateHousehold(int householdId, int actingUserId) {
        if (!canManage(actingUserId)) {
            return AuthorizationService.STAFF_ACCESS_DENIED;
        }
        try {
            Household household = householdRepo.getById(householdId).orElse(null);
            if (household == null) {
                return "Household record not found";
            }
            if (household.getHouseholdStatus() == HouseholdStatus.INACTIVE) {
                return "Household is already inactive";
            }
            if (!householdRepo.getMembers(householdId).isEmpty()) {
                return "Remove all household members before deactivating the household";
            }

            return householdRepo.deactivate(householdId)
                    ? "Household deactivated successfully"
                    : "Failed to deactivate household";
        } catch (SQLException exception) {
            return DATABASE_ERROR;
        }
    }

    private String validateHousehold(Household household, boolean requireId) {
        if (household == null) {
            return "Household information is required";
        }
        if (requireId && (household.getHouseholdId() == null || household.getHouseholdId() <= 0)) {
            return "Invalid household ID";
        }
        if (isBlank(household.getHouseholdCode()) ||
                isBlank(household.getAddressLine()) ||
                isBlank(household.getPurok())) {
            return "Household code, address, and purok are required";
        }
        if (household.getHouseholdStatus() == null) {
            return "Household status is required";
        }
        return null;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean canManage(int actingUserId) {
        return authorizationService.canAccessStaffOperations(actingUserId);
    }
}
