package com.joysistvi.brgyconnectapp.service;

import com.joysistvi.brgyconnectapp.model.ServiceType;
import com.joysistvi.brgyconnectapp.repository.ServiceTypeRepo;
import com.joysistvi.brgyconnectapp.repository.ServiceTypeRepoImpl;

import java.util.List;

// Business logic for ServiceType
public class ServiceTypeService {
    private final ServiceTypeRepo repo = new ServiceTypeRepoImpl();

    public List<ServiceType> getAvailableServices() { return repo.getAllActive(); }
    public List<ServiceType> getAllServices() { return repo.getAll(); }
    public ServiceType getServiceById(Integer id) { return repo.getById(id).orElse(null); }

    public String addServiceType(ServiceType type) {
        // Validate required fields
        if (type == null) {
            return "Service information is required";
        }

        if (type.getServiceCode() == null || type.getServiceCode().isBlank() ||
                type.getServiceName() == null || type.getServiceName().isBlank()) {
            return "Service code and name are required";
        }
        if (type.getDefaultFee() == null || type.getDefaultFee().signum() < 0)
            return "Valid service fee is required";
        if (type.getExpectedProcessingDays() <= 0)
            return "Processing days must be greater than zero";
        if (repo.getByCode(type.getServiceCode()).isPresent())
            return "Service code already exists";
        if (repo.getByName(type.getServiceName()).isPresent())
            return "Service name already exists";

        return repo.save(type) ? "Service added successfully" : "Failed to add service";
    }

    public String updateServiceType(ServiceType type) {
        if (type.getServiceTypeId() == null || type.getServiceTypeId() <= 0)
            return "Invalid service ID";
        return repo.update(type) ? "Service updated successfully" : "Failed to update service";
    }

    public String removeServiceType(Integer id) {
        if (id == null || id <= 0)
            return "Invalid service ID";
        return repo.deactivate(id) ? "Service marked as inactive" : "Failed to update status";
    }
}