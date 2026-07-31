package com.joysistvi.brgyconnectapp.controller;

import com.joysistvi.brgyconnectapp.model.Resident;
import com.joysistvi.brgyconnectapp.service.ResidentService;

import java.util.List;

/**
 * Controller layer for Resident module.
 * Follows the same constructor injection pattern as AuthController.
 */
public class ResidentController {

    private final ResidentService residentService;

    public ResidentController(ResidentService residentService) {
        this.residentService = residentService;
    }

    public List<Resident> getAllResidents() {
        return residentService.getAllResidents();
    }

    public Resident getResidentById(int id) {
        return residentService.getResidentById(id);
    }

    public String addResident(Resident resident) {
        return residentService.addResident(resident);
    }

    public String updateResident(Resident resident) {
        return residentService.updateResident(resident);
    }

    public String deleteResident(int id) {
        return residentService.deleteResident(id);
    }
}