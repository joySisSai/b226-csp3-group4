package com.joysistvi.brgyconnectapp.service;

import com.joysistvi.brgyconnectapp.model.ServiceType;
import com.joysistvi.brgyconnectapp.repository.ServiceTypeRepo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;

public class ServiceTypeAdminService {
    private static final BigDecimal MAXIMUM_FEE = new BigDecimal("99999999.99");
    private static final int MAXIMUM_PROCESSING_DAYS = 65_535;
    private static final String DATABASE_ERROR =
            "Unable to complete the operation because the database is unavailable";

    private final ServiceTypeRepo serviceTypeRepo;
    private final AuthorizationService authorizationService;

    public ServiceTypeAdminService(ServiceTypeRepo serviceTypeRepo,
                                   AuthorizationService authorizationService) {
        this.serviceTypeRepo = serviceTypeRepo;
        this.authorizationService = authorizationService;
    }

    public List<ServiceType> getAllServiceTypes(int actingAdminId) {
        if (!canManage(actingAdminId)) {
            return List.of();
        }
        try {
            return serviceTypeRepo.getAll();
        } catch (SQLException exception) {
            return List.of();
        }
    }

    public ServiceType getById(int serviceTypeId, int actingAdminId) {
        if (!canManage(actingAdminId)) {
            return null;
        }
        if (serviceTypeId <= 0) {
            return null;
        }
        try {
            return serviceTypeRepo.getById(serviceTypeId).orElse(null);
        } catch (SQLException exception) {
            return null;
        }
    }

    public String createServiceType(ServiceType serviceType, int actingAdminId) {
        if (!canManage(actingAdminId)) {
            return AuthorizationService.ADMIN_ACCESS_DENIED;
        }
        String validationError = validate(serviceType, false);
        if (validationError != null) {
            return validationError;
        }

        normalize(serviceType);
        serviceType.setActive(true);
        try {
            if (serviceTypeRepo.getByCode(serviceType.getServiceCode()).isPresent()) {
                return "Service code already exists";
            }
            return serviceTypeRepo.save(serviceType)
                    ? "Service type created successfully"
                    : "Failed to create service type";
        } catch (SQLException exception) {
            return DATABASE_ERROR;
        }
    }

    public String updateServiceType(ServiceType serviceType, int actingAdminId) {
        if (!canManage(actingAdminId)) {
            return AuthorizationService.ADMIN_ACCESS_DENIED;
        }
        String validationError = validate(serviceType, true);
        if (validationError != null) {
            return validationError;
        }

        try {
            ServiceType existing = serviceTypeRepo.getById(serviceType.getServiceTypeId()).orElse(null);
            if (existing == null) {
                return "Service type not found";
            }
            serviceType.setServiceCode(existing.getServiceCode());
            serviceType.setActive(existing.isActive());
            normalize(serviceType);
            return serviceTypeRepo.update(serviceType)
                    ? "Service type updated successfully"
                    : "Failed to update service type";
        } catch (SQLException exception) {
            return DATABASE_ERROR;
        }
    }

    public String setActive(int serviceTypeId, boolean active, int actingAdminId) {
        if (!canManage(actingAdminId)) {
            return AuthorizationService.ADMIN_ACCESS_DENIED;
        }
        if (serviceTypeId <= 0) {
            return "Invalid service type ID";
        }
        try {
            ServiceType serviceType = serviceTypeRepo.getById(serviceTypeId).orElse(null);
            if (serviceType == null) {
                return "Service type not found";
            }
            if (serviceType.isActive() == active) {
                return active ? "Service type is already active" : "Service type is already inactive";
            }
            return serviceTypeRepo.setActive(serviceTypeId, active)
                    ? "Service type " + (active ? "activated" : "deactivated") + " successfully"
                    : "Failed to update service type status";
        } catch (SQLException exception) {
            return DATABASE_ERROR;
        }
    }

    private String validate(ServiceType serviceType, boolean requireId) {
        if (serviceType == null) {
            return "Service type information is required";
        }
        if (requireId && (serviceType.getServiceTypeId() == null || serviceType.getServiceTypeId() <= 0)) {
            return "Invalid service type ID";
        }
        if (isBlank(serviceType.getServiceCode()) || isBlank(serviceType.getServiceName())) {
            return "Service code and service name are required";
        }
        if (serviceType.getServiceCode().trim().length() > 30) {
            return "Service code must not exceed 30 characters";
        }
        if (serviceType.getServiceName().trim().length() > 120) {
            return "Service name must not exceed 120 characters";
        }
        if (serviceType.getDescription() != null && serviceType.getDescription().trim().length() > 500) {
            return "Description must not exceed 500 characters";
        }
        if (serviceType.getDefaultFee() == null ||
                serviceType.getDefaultFee().compareTo(BigDecimal.ZERO) < 0 ||
                serviceType.getDefaultFee().compareTo(MAXIMUM_FEE) > 0) {
            return "Default fee must be between 0.00 and " + MAXIMUM_FEE;
        }
        if (serviceType.getExpectedProcessingDays() < 0 ||
                serviceType.getExpectedProcessingDays() > MAXIMUM_PROCESSING_DAYS) {
            return "Expected processing days must be between 0 and " + MAXIMUM_PROCESSING_DAYS;
        }
        return null;
    }

    private void normalize(ServiceType serviceType) {
        serviceType.setServiceCode(serviceType.getServiceCode().trim().toUpperCase());
        serviceType.setServiceName(serviceType.getServiceName().trim());
        serviceType.setDescription(blankToNull(serviceType.getDescription()));
        serviceType.setDefaultFee(serviceType.getDefaultFee().setScale(2, RoundingMode.HALF_UP));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public boolean canManage(int actingAdminId) {
        return authorizationService.canAccessAdminOperations(actingAdminId);
    }
}
