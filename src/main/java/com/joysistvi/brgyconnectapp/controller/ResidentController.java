package com.joysistvi.brgyconnectapp.controller;

import com.joysistvi.brgyconnectapp.model.Resident;
import com.joysistvi.brgyconnectapp.service.ResidentService;

import java.util.List;

public class ResidentController {
    private final ResidentService service;

    public ResidentController() {
        this.service = new ResidentService();
    }

    public ResidentController(ResidentService service) {
        this.service = service;
    }
    public List<Resident> searchResidents(String keyword) {
        return service.searchResidents(keyword);
    }
    public List<Resident> getActiveResidents() { return service.getAllActive(); }
    public List<Resident> getAllResidents() { return service.getAllResidents(); }
    public Resident getById(Integer id) { return service.getResidentById(id); }
    public String register(Resident r) { return service.addResident(r); }
    public String update(Resident r) { return service.updateResident(r); }
    public String deactivate(Integer id) { return service.deactivateResident(id); }
}