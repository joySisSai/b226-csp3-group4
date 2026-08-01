package com.joysistvi.brgyconnectapp.controller;

import com.joysistvi.brgyconnectapp.model.Household;
import com.joysistvi.brgyconnectapp.model.Resident;
import com.joysistvi.brgyconnectapp.service.HouseholdService;

import java.util.List;

public class HouseholdController {
    private final HouseholdService householdService;

    public HouseholdController(HouseholdService householdService) {
        this.householdService = householdService;
    }

    public List<Household> getAllHouseholds() {
        return householdService.getAllHouseholds();
    }

    public Household getById(int householdId) {
        return householdService.getHouseholdById(householdId);
    }

    public List<Household> search(String keyword) {
        return householdService.searchHouseholds(keyword);
    }

    public List<Resident> getMembers(int householdId) {
        return householdService.getMembers(householdId);
    }

    public String create(Household household) {
        return householdService.createHousehold(household);
    }

    public String update(Household household) {
        return householdService.updateHousehold(household);
    }

    public String addMember(int householdId, int residentId) {
        return householdService.addMember(householdId, residentId);
    }

    public String removeMember(int householdId, int residentId) {
        return householdService.removeMember(householdId, residentId);
    }

    public String assignHouseholdHead(int householdId, int residentId) {
        return householdService.assignHouseholdHead(householdId, residentId);
    }

    public String deactivate(int householdId) {
        return householdService.deactivateHousehold(householdId);
    }
}
