package com.joysistvi.brgyconnectapp.service;

import com.joysistvi.brgyconnectapp.model.RequestStatus;
import com.joysistvi.brgyconnectapp.model.ServiceRequest;
import com.joysistvi.brgyconnectapp.repository.ResidentRepo;
import com.joysistvi.brgyconnectapp.repository.ResidentRepoImpl;
import com.joysistvi.brgyconnectapp.repository.ServiceRequestRepo;
import com.joysistvi.brgyconnectapp.repository.ServiceRequestRepoImpl;

import java.time.LocalDate;
import java.util.List;

public class ServiceRequestService {
    private final ServiceRequestRepo repo = new ServiceRequestRepoImpl();
    private final ResidentRepo residentRepo = new ResidentRepoImpl();

    public List<ServiceRequest> getAllRequests() { return repo.getAllRequests(); }
    public List<ServiceRequest> getRequestsByResident(Integer residentId) { return repo.getByResidentId(residentId); }
    public ServiceRequest getRequestById(Long id) { return repo.getById(id); }

    // Submit new request — validates resident exists and required fields
    public String submitRequest(ServiceRequest request) {
        // Validate required fields
        if (request.getResidentId() == null || request.getResidentId() <= 0)
            return "Resident ID is required";
        if (request.getServiceTypeId() == null || request.getServiceTypeId() <= 0)
            return "Service type is required";
        if (request.getPurpose() == null || request.getPurpose().isBlank())
            return "Purpose/description is required";

        // Validate resident record exists
        if (residentRepo.getById(request.getResidentId()).isEmpty())
            return "Resident record not found";

        // Set defaults if missing
        if (request.getRequestDate() == null) request.setRequestDate(LocalDate.now());
        if (request.getStatus() == null) request.setStatus(RequestStatus.PENDING);

        return repo.create(request) ? "Request submitted successfully" : "Failed to submit request — duplicate or invalid data";
    }

    // Update request status and details
    public String updateRequest(ServiceRequest request) {
        if (request.getRequestId() == null || request.getRequestId() <= 0)
            return "Invalid request ID";
        return repo.update(request) ? "Request updated successfully" : "Failed to update request";
    }
}