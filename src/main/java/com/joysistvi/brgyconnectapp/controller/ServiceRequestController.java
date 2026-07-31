package com.joysistvi.brgyconnectapp.controller;

import com.joysistvi.brgyconnectapp.model.ServiceRequest;
import com.joysistvi.brgyconnectapp.service.ServiceRequestService;

import java.util.List;

public class ServiceRequestController {
    private final ServiceRequestService service;

    public ServiceRequestController(ServiceRequestService service) {
        this.service = service;
    }

    public List<ServiceRequest> getAllRequests() { return service.getAllRequests(); }
    public List<ServiceRequest> getMyRequests(Integer residentId) { return service.getRequestsByResident(residentId); }
    public ServiceRequest getRequest(Long id) { return service.getRequestById(id); }
    public String submit(ServiceRequest request) { return service.submitRequest(request); }
    public String update(ServiceRequest request) { return service.updateRequest(request); }
}