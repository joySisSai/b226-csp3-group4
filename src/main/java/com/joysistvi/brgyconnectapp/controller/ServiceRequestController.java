package com.joysistvi.brgyconnectapp.controller;

import com.joysistvi.brgyconnectapp.model.RequestStatus;
import com.joysistvi.brgyconnectapp.model.RequestStatusHistory;
import com.joysistvi.brgyconnectapp.model.ServiceRequest;
import com.joysistvi.brgyconnectapp.model.ServiceType;
import com.joysistvi.brgyconnectapp.service.ServiceRequestService;

import java.util.List;

public class ServiceRequestController {
    private final ServiceRequestService requestService;

    public ServiceRequestController(ServiceRequestService requestService) {
        this.requestService = requestService;
    }

    public List<ServiceRequest> search(String keyword) {
        return requestService.searchRequests(keyword);
    }

    public ServiceRequest getById(long requestId) {
        return requestService.getRequestById(requestId);
    }

    public List<RequestStatusHistory> getHistory(long requestId) {
        return requestService.getStatusHistory(requestId);
    }

    public List<ServiceType> getActiveServiceTypes() {
        return requestService.getActiveServiceTypes();
    }

    public String create(ServiceRequest request) {
        return requestService.createRequest(request);
    }

    public String updateStatus(long requestId,
                               RequestStatus newStatus,
                               String remarks,
                               int changedByUserId) {
        return requestService.updateStatus(requestId, newStatus, remarks, changedByUserId);
    }

    public List<RequestStatus> getAllowedTransitions(RequestStatus currentStatus) {
        return requestService.getAllowedTransitions(currentStatus);
    }
}
