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

    public List<ServiceRequest> search(String keyword, int actingUserId) {
        return requestService.searchRequests(keyword, actingUserId);
    }

    public ServiceRequest getById(long requestId, int actingUserId) {
        return requestService.getRequestById(requestId, actingUserId);
    }

    public List<RequestStatusHistory> getHistory(long requestId, int actingUserId) {
        return requestService.getStatusHistory(requestId, actingUserId);
    }

    public List<ServiceType> getActiveServiceTypes(int actingUserId) {
        return requestService.getActiveServiceTypes(actingUserId);
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

    public List<ServiceRequest> getOwnRequests(int residentId, int actingUserId) {
        return requestService.getOwnRequests(residentId, actingUserId);
    }

    public ServiceRequest getOwnRequestById(long requestId, int residentId, int actingUserId) {
        return requestService.getOwnRequestById(requestId, residentId, actingUserId);
    }

    public List<RequestStatusHistory> getOwnStatusHistory(long requestId, int residentId, int actingUserId) {
        return requestService.getOwnStatusHistory(requestId, residentId, actingUserId);
    }

    public List<ServiceType> getActiveServiceTypesForResident(int residentId, int actingUserId) {
        return requestService.getActiveServiceTypesForResident(residentId, actingUserId);
    }

    public String createOwnRequest(ServiceRequest request, int residentId, int actingUserId) {
        String result = requestService.createOwnRequest(request, residentId, actingUserId);
        if (result != null && result.endsWith("submitted successfully") && activityLogService != null) {
            activityLogService.record(
                    actingUserId,
                    "CREATE",
                    "SERVICE_REQUEST",
                    request.getRequestId(),
                    "Resident submitted service request " + request.getRequestNumber() + "."
            );
        }
        return result;
    }

    public String cancelOwnRequest(long requestId, int residentId, int actingUserId) {
        ServiceRequest existing = requestService.getOwnRequestById(requestId, residentId, actingUserId);
        String result = requestService.cancelOwnRequest(requestId, residentId, actingUserId);
        if ("Request cancelled successfully".equals(result) && activityLogService != null) {
            String requestLabel = existing == null || existing.getRequestNumber() == null
                    ? String.valueOf(requestId)
                    : existing.getRequestNumber();
            activityLogService.record(
                    actingUserId,
                    "CANCEL_REQUEST",
                    "SERVICE_REQUEST",
                    requestId,
                    "Resident cancelled pending service request " + requestLabel + "."
            );
        }
        return result;
    }

    public String updateStatus(long requestId,
                               RequestStatus newStatus,
                               String remarks,
                               int changedByUserId) {
        ServiceRequest existing = requestService.getRequestById(requestId, changedByUserId);
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
