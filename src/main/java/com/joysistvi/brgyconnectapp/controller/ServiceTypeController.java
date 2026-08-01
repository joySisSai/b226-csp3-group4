package com.joysistvi.brgyconnectapp.controller;

import com.joysistvi.brgyconnectapp.model.ServiceRequest;
import com.joysistvi.brgyconnectapp.model.ServiceType;
import com.joysistvi.brgyconnectapp.service.ServiceTypeService;

import java.util.List;

public class ServiceTypeController {
    private final ServiceTypeService service = new ServiceTypeService();

    public List<ServiceType> getAvailableServices() { return service.getAvailableServices(); }
    public List<ServiceType> getAllServices() { return service.getAllServices(); }
    public ServiceType getServiceById(Integer id) { return service.getServiceById(id); }
    public String addService(ServiceType type) { return service.addServiceType(type); }
    public String updateService(ServiceType type) { return service.updateServiceType(type); }
    public String deactivateService(Integer id) { return service.removeServiceType(id); }

}