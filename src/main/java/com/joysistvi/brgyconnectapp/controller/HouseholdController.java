package com.joysistvi.brgyconnectapp.controller;

import com.joysistvi.brgyconnectapp.model.Household;
import com.joysistvi.brgyconnectapp.model.Resident;
import com.joysistvi.brgyconnectapp.service.ActivityLogService;
import com.joysistvi.brgyconnectapp.service.HouseholdService;

import java.util.List;

public class HouseholdController {
    private final HouseholdService householdService;
    private final ActivityLogService activityLogService;

    public HouseholdController(HouseholdService householdService) {
        this.householdService = householdService;
        this.activityLogService = null;
    }

    public HouseholdController(HouseholdService householdService, ActivityLogService activityLogService) {
        this.householdService = householdService;
        this.activityLogService = activityLogService;
    }

    public List<Household> getAllHouseholds(int actingUserId) {
        return householdService.getAllHouseholds(actingUserId);
    }

    public Household getById(int householdId, int actingUserId) {
        return householdService.getHouseholdById(householdId, actingUserId);
    }

    public List<Household> search(String keyword, int actingUserId) {
        return householdService.searchHouseholds(keyword, actingUserId);
    }

    public List<Resident> getMembers(int householdId, int actingUserId) {
        return householdService.getMembers(householdId, actingUserId);
    }

    public String create(Household household, int actingUserId) {
        String result = householdService.createHousehold(household, actingUserId);
        if ("Household created successfully".equals(result)) {
            record(actingUserId, "CREATE", household.getHouseholdId(),
                    "Created household " + household.getHouseholdCode() + ".");
        }
        return result;
    }

    public String update(Household household, int actingUserId) {
        String result = householdService.updateHousehold(household, actingUserId);
        if ("Household updated successfully".equals(result)) {
            record(actingUserId, "UPDATE", household.getHouseholdId(),
                    "Updated household " + household.getHouseholdCode() + ".");
        }
        return result;
    }

    public String addMember(int householdId, int residentId, int actingUserId) {
        String result = householdService.addMember(householdId, residentId, actingUserId);
        if ("Resident added to household successfully".equals(result)) {
            record(actingUserId, "ADD_MEMBER", householdId,
                    "Added resident " + residentId + " to household " + householdId + ".");
        }
        return result;
    }

    public String removeMember(int householdId, int residentId, int actingUserId) {
        String result = householdService.removeMember(householdId, residentId, actingUserId);
        if ("Resident removed from household successfully".equals(result)) {
            record(actingUserId, "REMOVE_MEMBER", householdId,
                    "Removed resident " + residentId + " from household " + householdId + ".");
        }
        return result;
    }

    public String assignHouseholdHead(int householdId, int residentId, int actingUserId) {
        String result = householdService.assignHouseholdHead(householdId, residentId, actingUserId);
        if ("Household head assigned successfully".equals(result)) {
            record(actingUserId, "ASSIGN_HEAD", householdId,
                    "Assigned resident " + residentId + " as head of household " + householdId + ".");
        }
        return result;
    }

    public String deactivate(int householdId, int actingUserId) {
        String result = householdService.deactivateHousehold(householdId, actingUserId);
        if ("Household deactivated successfully".equals(result)) {
            record(actingUserId, "DEACTIVATE", householdId,
                    "Deactivated household " + householdId + ".");
        }
        return result;
    }

    private void record(int actingUserId, String action, Integer householdId, String description) {
        if (activityLogService != null) {
            activityLogService.record(actingUserId, action, "HOUSEHOLD",
                    householdId == null ? null : householdId.longValue(), description);
        }
    }
}
