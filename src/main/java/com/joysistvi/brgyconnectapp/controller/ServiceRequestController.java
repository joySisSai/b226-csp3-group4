package com.joysistvi.brgyconnectapp.controller;

import com.joysistvi.brgyconnectapp.model.RequestStatus;
import com.joysistvi.brgyconnectapp.model.RequestStatusHistory;
import com.joysistvi.brgyconnectapp.model.ServiceRequest;
import com.joysistvi.brgyconnectapp.model.ServiceType;
import com.joysistvi.brgyconnectapp.service.ActivityLogService;
import com.joysistvi.brgyconnectapp.service.ServiceRequestService;

import java.util.List;

public class ServiceRequestController {
    private final ServiceRequestService requestService;
    private final ActivityLogService activityLogService;

    public ServiceRequestController(ServiceRequestService requestService) {
        this.requestService = requestService;
        this.activityLogService = null;
    }

    public ServiceRequestController(ServiceRequestService requestService,
                                    ActivityLogService activityLogService) {
        this.requestService = requestService;
        this.activityLogService = activityLogService;
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
        String result = requestService.createRequest(request);
        if (result != null && result.endsWith("created successfully") && activityLogService != null) {
            activityLogService.record(
                    request.getCreatedByUserId(),
                    "CREATE",
                    "SERVICE_REQUEST",
                    request.getRequestId(),
                    "Created service request " + request.getRequestNumber() + "."
            );
        }
        return result;
    }

    public String updateStatus(long requestId,
                               RequestStatus newStatus,
                               String remarks,
                               int changedByUserId) {
        ServiceRequest existing = requestService.getRequestById(requestId);
        String result = requestService.updateStatus(requestId, newStatus, remarks, changedByUserId);
        if ("Request status updated successfully".equals(result) && activityLogService != null) {
            String requestLabel = existing == null || existing.getRequestNumber() == null
                    ? String.valueOf(requestId)
                    : existing.getRequestNumber();
            String oldStatus = existing == null || existing.getStatus() == null
                    ? "UNKNOWN"
                    : existing.getStatus().name();
            activityLogService.record(
                    changedByUserId,
                    "UPDATE_STATUS",
                    "SERVICE_REQUEST",
                    requestId,
                    "Changed request " + requestLabel + " status from " + oldStatus + " to " + newStatus + "."
            );
        }
        return result;
    }

    public List<RequestStatus> getAllowedTransitions(RequestStatus currentStatus) {
        return requestService.getAllowedTransitions(currentStatus);
    }
}
