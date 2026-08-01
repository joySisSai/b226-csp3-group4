package com.joysistvi.brgyconnectapp.controller;

import com.joysistvi.brgyconnectapp.model.Resident;
import com.joysistvi.brgyconnectapp.service.ActivityLogService;
import com.joysistvi.brgyconnectapp.service.ResidentService;

import java.util.List;

public class ResidentController {
    private final ResidentService service;
    private final ActivityLogService activityLogService;

    public ResidentController() {
        this.service = new ResidentService();
        this.activityLogService = null;
    }

    public ResidentController(ResidentService service) {
        this.service = service;
        this.activityLogService = null;
    }

    public ResidentController(ResidentService service, ActivityLogService activityLogService) {
        this.service = service;
        this.activityLogService = activityLogService;
    }
    public List<Resident> searchResidents(String keyword) {
        return service.searchResidents(keyword);
    }
    public List<Resident> getActiveResidents() { return service.getAllActive(); }
    public List<Resident> getAllResidents() { return service.getAllResidents(); }
    public Resident getById(Integer id) { return service.getResidentById(id); }
    public String register(Resident resident, int actingUserId) {
        String result = service.addResident(resident);
        if ("Resident added successfully".equals(result)) {
            record(actingUserId, "CREATE", resident.getResidentId(),
                    "Created resident record " + resident.getResidentCode() + ".");
        }
        return result;
    }

    public String update(Resident resident, int actingUserId) {
        String result = service.updateResident(resident);
        if ("Resident updated successfully".equals(result)) {
            record(actingUserId, "UPDATE", resident.getResidentId(),
                    "Updated resident record " + resident.getResidentCode() + ".");
        }
        return result;
    }

    public String deactivate(Integer residentId, int actingUserId) {
        String result = service.deactivateResident(residentId);
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
}
