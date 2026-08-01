package com.joysistvi.brgyconnectapp.controller;

import com.joysistvi.brgyconnectapp.model.Resident;
import com.joysistvi.brgyconnectapp.service.ResidentService;

import java.util.List;

// Middleman between UI and Service — receives requests and returns results
public class ResidentController {
    private final ResidentService residentService;

    public ResidentController(ResidentService residentService) {
        this.residentService = residentService;
    }

    public List<Resident> getAllResidents() { return residentService.getAllResidents(); }
    public Resident getResidentById(int id) { return residentService.getResidentById(id); }
    public String addResident(Resident resident) { return residentService.addResident(resident); }
    public String updateResident(Resident resident) { return residentService.updateResident(resident); }
    public String deactivateResident(int id) { return residentService.deactivateResident(id); }
}