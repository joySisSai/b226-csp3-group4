package com.joysistvi.brgyconnectapp.controller;

import com.joysistvi.brgyconnectapp.model.ServiceType;
import com.joysistvi.brgyconnectapp.service.ServiceTypeAdminService;

import java.util.List;

public class ServiceTypeAdminController {
    private final ServiceTypeAdminService serviceTypeService;

    public ServiceTypeAdminController(ServiceTypeAdminService serviceTypeService) {
        this.serviceTypeService = serviceTypeService;
    }

    public List<ServiceType> getAll() {
        return serviceTypeService.getAllServiceTypes();
    }

    public ServiceType getById(int serviceTypeId) {
        return serviceTypeService.getById(serviceTypeId);
    }

    public String create(ServiceType serviceType) {
        return serviceTypeService.createServiceType(serviceType);
    }

    public String update(ServiceType serviceType) {
        return serviceTypeService.updateServiceType(serviceType);
    }

    public String setActive(int serviceTypeId, boolean active) {
        return serviceTypeService.setActive(serviceTypeId, active);
    }
}
