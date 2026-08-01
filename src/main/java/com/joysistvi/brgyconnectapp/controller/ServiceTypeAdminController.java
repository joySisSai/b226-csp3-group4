package com.joysistvi.brgyconnectapp.controller;

import com.joysistvi.brgyconnectapp.model.ServiceType;
import com.joysistvi.brgyconnectapp.service.ActivityLogService;
import com.joysistvi.brgyconnectapp.service.ServiceTypeAdminService;

import java.util.List;

public class ServiceTypeAdminController {
    private final ServiceTypeAdminService serviceTypeService;
    private final ActivityLogService activityLogService;

    public ServiceTypeAdminController(ServiceTypeAdminService serviceTypeService) {
        this.serviceTypeService = serviceTypeService;
        this.activityLogService = null;
    }

    public ServiceTypeAdminController(ServiceTypeAdminService serviceTypeService,
                                      ActivityLogService activityLogService) {
        this.serviceTypeService = serviceTypeService;
        this.activityLogService = activityLogService;
    }

    public List<ServiceType> getAll(int actingAdminId) {
        return serviceTypeService.getAllServiceTypes(actingAdminId);
    }

    public ServiceType getById(int serviceTypeId, int actingAdminId) {
        return serviceTypeService.getById(serviceTypeId, actingAdminId);
    }

    public String create(ServiceType serviceType, int actingUserId) {
        String result = serviceTypeService.createServiceType(serviceType, actingUserId);
        if ("Service type created successfully".equals(result)) {
            record(actingUserId, "CREATE", serviceType,
                    "Created service type " + serviceType.getServiceCode() + ".");
        }
        return result;
    }

    public String update(ServiceType serviceType, int actingUserId) {
        String result = serviceTypeService.updateServiceType(serviceType, actingUserId);
        if ("Service type updated successfully".equals(result)) {
            record(actingUserId, "UPDATE", serviceType,
                    "Updated service type " + serviceType.getServiceCode() + ".");
        }
        return result;
    }

    public String setActive(int serviceTypeId, boolean active, int actingUserId) {
        String result = serviceTypeService.setActive(serviceTypeId, active, actingUserId);
        if (result.endsWith("successfully") && activityLogService != null) {
            activityLogService.record(actingUserId, active ? "ACTIVATE" : "DEACTIVATE",
                    "SERVICE_TYPE", (long) serviceTypeId,
                    (active ? "Activated" : "Deactivated") + " service type " + serviceTypeId + ".");
        }
        return result;
    }

    private void record(int actingUserId, String action, ServiceType serviceType, String description) {
        if (activityLogService != null) {
            Integer id = serviceType.getServiceTypeId();
            activityLogService.record(actingUserId, action, "SERVICE_TYPE",
                    id == null ? null : id.longValue(), description);
        }
    }
}
