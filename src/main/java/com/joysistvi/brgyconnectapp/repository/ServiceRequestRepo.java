package com.joysistvi.brgyconnectapp.repository;

import com.joysistvi.brgyconnectapp.model.ServiceRequest;
import java.util.List;

public interface ServiceRequestRepo {
    List<ServiceRequest> getAllRequests();
    List<ServiceRequest> getByResidentId(Integer residentId);
    ServiceRequest getById(Long id);
    boolean create(ServiceRequest request);
    boolean update(ServiceRequest request);
}