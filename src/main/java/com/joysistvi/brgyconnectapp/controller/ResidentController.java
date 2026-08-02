package com.joysistvi.brgyconnectapp.controller;

import com.joysistvi.brgyconnectapp.model.Resident;
import com.joysistvi.brgyconnectapp.model.User;
import com.joysistvi.brgyconnectapp.service.ActivityLogService;
import com.joysistvi.brgyconnectapp.service.ResidentService;

import java.util.List;

public class ResidentController {
    private final ResidentService service;
    private final ActivityLogService activityLogService;

    public ResidentController(ResidentService service) {
        this.service = service;
        this.activityLogService = null;
    }

    public ResidentController(ResidentService service, ActivityLogService activityLogService) {
        this.service = service;
        this.activityLogService = activityLogService;
    }
    public List<Resident> searchResidents(String keyword, int actingUserId) {
        return service.searchResidents(keyword, actingUserId);
    }
    public List<Resident> getActiveResidents(int actingUserId) {
        return service.getAllActive(actingUserId);
    }
    public List<Resident> getAllResidents(int actingUserId) {
        return service.getAllResidents(actingUserId);
    }

    public List<Resident> getResidents(int actingUserId, int offset, int limit) {
        return service.getResidents(offset, limit, actingUserId);
    }

    public Resident getById(Integer id, int actingUserId) {
        return service.getResidentById(id, actingUserId);
    }
    public Resident getOwnProfile(Integer residentId, int actingUserId) {
        return residentId == null ? null : service.getOwnResidentProfile(residentId, actingUserId);
    }
    public String register(Resident resident, int actingUserId) {
        String result = service.addResident(resident, actingUserId);
        if (result != null && result.startsWith("Resident added successfully")) {
            record(actingUserId, "CREATE", resident.getResidentId(),
                    "Created resident record " + resident.getResidentCode() + ".");
        }
        return result;
    }

    public String update(Resident resident, int actingUserId) {
        String result = service.updateResident(resident, actingUserId);
        if ("Resident updated successfully".equals(result)) {
            record(actingUserId, "UPDATE", resident.getResidentId(),
                    "Updated resident record " + resident.getResidentCode() + ".");
        }
        return result;
    }

    public String deactivate(Integer residentId, int actingUserId) {
        String result = service.deactivateResident(residentId, actingUserId);
        if ("Resident marked as inactive".equals(result)) {
            record(actingUserId, "DEACTIVATE", residentId,
                    "Marked resident record " + residentId + " as inactive.");
        }
        return result;
    }

    private void record(int actingUserId, String action, Integer residentId, String description) {
        if (activityLogService != null) {
            activityLogService.record(actingUserId, action, "RESIDENT",
                    residentId == null ? null : residentId.longValue(), description);
        }
    }

    public List<User> getPendingAccounts(int actingUserId) {
        return service.getPendingAccounts(actingUserId);
    }

    public String approveResidentAccount(int targetUserId, int actingUserId) {
        String message = service.approveResidentAccount(targetUserId, actingUserId);
        if (message.toLowerCase().contains("success")) {
            if (activityLogService != null) {
                activityLogService.record(actingUserId, "APPROVE_ACCOUNT", "USER", (long) targetUserId,
                        "Approved resident account ID " + targetUserId);
            }
        }
        return message;
    }

    public String rejectResidentAccount(int targetUserId, int actingUserId) {
        String message = service.rejectResidentAccount(targetUserId, actingUserId);
        if (message.toLowerCase().contains("success") || message.toLowerCase().contains("rejected")) {
            if (activityLogService != null) {
                activityLogService.record(actingUserId, "REJECT_ACCOUNT", "USER", (long) targetUserId,
                        "Rejected resident account ID " + targetUserId);
            }
        }
        return message;
    }
}
