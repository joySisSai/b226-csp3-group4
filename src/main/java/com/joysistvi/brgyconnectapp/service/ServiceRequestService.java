package com.joysistvi.brgyconnectapp.service;

import com.joysistvi.brgyconnectapp.model.RequestStatus;
import com.joysistvi.brgyconnectapp.model.RequestStatusHistory;
import com.joysistvi.brgyconnectapp.model.Resident;
import com.joysistvi.brgyconnectapp.model.ResidencyStatus;
import com.joysistvi.brgyconnectapp.model.ServiceRequest;
import com.joysistvi.brgyconnectapp.model.ServiceType;
import com.joysistvi.brgyconnectapp.repository.ResidentRepo;
import com.joysistvi.brgyconnectapp.repository.ServiceRequestRepo;
import com.joysistvi.brgyconnectapp.repository.ServiceTypeRepo;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class ServiceRequestService {
    private static final int MAXIMUM_RESULTS = 100;
    private static final String DATABASE_ERROR =
            "Unable to complete the operation because the database is unavailable";

    private final ServiceRequestRepo requestRepo;
    private final ServiceTypeRepo serviceTypeRepo;
    private final ResidentRepo residentRepo;
    private final AuthorizationService authorizationService;

    public ServiceRequestService(ServiceRequestRepo requestRepo,
                                 ServiceTypeRepo serviceTypeRepo,
                                 ResidentRepo residentRepo,
                                 AuthorizationService authorizationService) {
        this.requestRepo = requestRepo;
        this.serviceTypeRepo = serviceTypeRepo;
        this.residentRepo = residentRepo;
        this.authorizationService = authorizationService;
    }

    public List<ServiceRequest> getRecentRequests(int actingUserId) {
        if (!canManage(actingUserId)) {
            return List.of();
        }
        try {
            return requestRepo.getRecent(MAXIMUM_RESULTS);
        } catch (SQLException exception) {
            throw new DataAccessException(exception);
        }
    }

    public List<ServiceRequest> searchRequests(String keyword, int actingUserId) {
        if (!canManage(actingUserId)) {
            return List.of();
        }
        if (keyword == null || keyword.isBlank()) {
            return getRecentRequests(actingUserId);
        }
        try {
            return requestRepo.search(keyword.trim(), MAXIMUM_RESULTS);
        } catch (SQLException exception) {
            throw new DataAccessException(exception);
        }
    }

    public ServiceRequest getRequestById(long requestId, int actingUserId) {
        if (!canManage(actingUserId)) {
            return null;
        }
        if (requestId <= 0) {
            return null;
        }
        try {
            return requestRepo.getById(requestId).orElse(null);
        } catch (SQLException exception) {
            throw new DataAccessException(exception);
        }
    }

    public List<RequestStatusHistory> getStatusHistory(long requestId, int actingUserId) {
        if (!canManage(actingUserId)) {
            return List.of();
        }
        if (requestId <= 0) {
            return List.of();
        }
        try {
            return requestRepo.getStatusHistory(requestId);
        } catch (SQLException exception) {
            throw new DataAccessException(exception);
        }
    }

    public List<ServiceType> getActiveServiceTypes(int actingUserId) {
        if (!canManage(actingUserId)) {
            return List.of();
        }
        try {
            return serviceTypeRepo.getAllActive();
        } catch (SQLException exception) {
            throw new DataAccessException(exception);
        }
    }

    public String createRequest(ServiceRequest request) {
        int actingUserId = request == null || request.getCreatedByUserId() == null
                ? 0
                : request.getCreatedByUserId();
        if (!canManage(actingUserId)) {
            return AuthorizationService.STAFF_ACCESS_DENIED;
        }
        String validationError = validateNewRequest(request);
        if (validationError != null) {
            return validationError;
        }

        try {
            Resident resident = residentRepo.getById(request.getResidentId()).orElse(null);
            if (resident == null) {
                return "Resident record not found";
            }
            if (resident.getResidencyStatus() != ResidencyStatus.ACTIVE) {
                return "Only active residents can create service requests";
            }

            ServiceType serviceType = serviceTypeRepo.getById(request.getServiceTypeId()).orElse(null);
            if (serviceType == null || !serviceType.isActive()) {
                return "The selected service type is unavailable";
            }

            request.setRequestNumber(generateRequestNumber());
            request.setRequestDate(LocalDate.now());
            request.setServiceFeeSnapshot(serviceType.getDefaultFee());
            request.setStatus(RequestStatus.PENDING);
            long requestId = requestRepo.saveWithInitialHistory(request);
            request.setRequestId(requestId);
            return "Service request " + request.getRequestNumber() + " created successfully";
        } catch (SQLException exception) {
            return DATABASE_ERROR;
        }
    }

    public String updateStatus(long requestId,
                               RequestStatus newStatus,
                               String remarks,
                               int changedByUserId) {
        if (!canManage(changedByUserId)) {
            return AuthorizationService.STAFF_ACCESS_DENIED;
        }
        if (requestId <= 0) {
            return "Invalid request ID";
        }
        if (newStatus == null) {
            return "A new request status is required";
        }
        if (changedByUserId <= 0) {
            return "A valid staff or administrator account is required";
        }
        if (remarks != null && remarks.trim().length() > 1000) {
            return "Remarks must not exceed 1000 characters";
        }
        if ((newStatus == RequestStatus.REJECTED || newStatus == RequestStatus.CANCELLED) &&
                (remarks == null || remarks.isBlank())) {
            return "Remarks are required when rejecting or cancelling a request";
        }

        try {
            ServiceRequest request = requestRepo.getById(requestId).orElse(null);
            if (request == null) {
                return "Service request not found";
            }
            if (!getAllowedTransitions(request.getStatus()).contains(newStatus)) {
                return "Cannot change request status from " + request.getStatus() + " to " + newStatus;
            }

            boolean updated = requestRepo.updateStatus(
                    requestId,
                    request.getStatus(),
                    newStatus,
                    blankToNull(remarks),
                    changedByUserId
            );
            return updated
                    ? "Request status updated successfully"
                    : "Request status changed before this update; reload the request and try again";
        } catch (SQLException exception) {
            return DATABASE_ERROR;
        }
    }

    public List<RequestStatus> getAllowedTransitions(RequestStatus currentStatus) {
        if (currentStatus == null) {
            return List.of();
        }
        return switch (currentStatus) {
            case PENDING -> List.of(RequestStatus.UNDER_REVIEW, RequestStatus.CANCELLED);
            case UNDER_REVIEW -> List.of(
                    RequestStatus.APPROVED,
                    RequestStatus.REJECTED,
                    RequestStatus.CANCELLED
            );
            case APPROVED -> List.of(RequestStatus.RELEASED, RequestStatus.CANCELLED);
            case RELEASED, REJECTED, CANCELLED -> List.of();
        };
    }

    public List<ServiceRequest> getOwnRequests(int residentId, int actingUserId) {
        if (!authorizationService.canViewOwnResidentProfile(actingUserId, residentId)) {
            return List.of();
        }
        try {
            return requestRepo.getOwn(residentId, MAXIMUM_RESULTS);
        } catch (SQLException exception) {
            throw new DataAccessException(exception);
        }
    }

    public ServiceRequest getOwnRequestById(long requestId, int residentId, int actingUserId) {
        if (!authorizationService.canViewOwnResidentProfile(actingUserId, residentId)) {
            return null;
        }
        try {
            ServiceRequest request = requestRepo.getById(requestId).orElse(null);
            if (request != null && request.getResidentId() == residentId) {
                return request;
            }
            return null;
        } catch (SQLException exception) {
            throw new DataAccessException(exception);
        }
    }

    public List<RequestStatusHistory> getOwnStatusHistory(long requestId, int residentId, int actingUserId) {
        if (!authorizationService.canViewOwnResidentProfile(actingUserId, residentId)) {
            return List.of();
        }
        try {
            ServiceRequest request = requestRepo.getById(requestId).orElse(null);
            if (request != null && request.getResidentId() == residentId) {
                return requestRepo.getStatusHistory(requestId);
            }
            return List.of();
        } catch (SQLException exception) {
            throw new DataAccessException(exception);
        }
    }

    public List<ServiceType> getActiveServiceTypesForResident(int residentId, int actingUserId) {
        if (!authorizationService.canViewOwnResidentProfile(actingUserId, residentId)) {
            return List.of();
        }
        try {
            return serviceTypeRepo.getAllActive();
        } catch (SQLException exception) {
            throw new DataAccessException(exception);
        }
    }

    public String createOwnRequest(ServiceRequest request, int residentId, int actingUserId) {
        if (!authorizationService.canViewOwnResidentProfile(actingUserId, residentId)) {
            return "Access denied: you can only create requests for your own profile";
        }
        
        String validationError = validateNewOwnRequest(request);
        if (validationError != null) {
            return validationError;
        }

        try {
            Resident resident = residentRepo.getById(residentId).orElse(null);
            if (resident == null) {
                return "Resident record not found";
            }
            if (resident.getResidencyStatus() != ResidencyStatus.ACTIVE) {
                return "Only active residents can create service requests";
            }

            ServiceType serviceType = serviceTypeRepo.getById(request.getServiceTypeId()).orElse(null);
            if (serviceType == null || !serviceType.isActive()) {
                return "The selected service type is unavailable";
            }

            request.setResidentId(residentId);
            request.setCreatedByUserId(actingUserId);
            request.setRequestNumber(generateRequestNumber());
            request.setRequestDate(LocalDate.now());
            request.setServiceFeeSnapshot(serviceType.getDefaultFee());
            request.setStatus(RequestStatus.PENDING);
            long requestId = requestRepo.saveWithInitialHistory(request);
            request.setRequestId(requestId);
            return "Service request " + request.getRequestNumber() + " submitted successfully";
        } catch (SQLException exception) {
            return DATABASE_ERROR;
        }
    }

    private String validateNewOwnRequest(ServiceRequest request) {
        if (request == null) {
            return "Service request information is required";
        }
        if (request.getServiceTypeId() == null || request.getServiceTypeId() <= 0) {
            return "A valid service type is required";
        }
        if (request.getPurpose() == null || request.getPurpose().isBlank()) {
            return "Request purpose is required";
        }
        if (request.getPurpose().trim().length() > 500) {
            return "Request purpose must not exceed 500 characters";
        }
        request.setPurpose(request.getPurpose().trim());
        request.setRemarks(null);
        return null;
    }

    private String validateNewRequest(ServiceRequest request) {
        if (request == null) {
            return "Service request information is required";
        }
        if (request.getResidentId() == null || request.getResidentId() <= 0) {
            return "A valid resident ID is required";
        }
        if (request.getServiceTypeId() == null || request.getServiceTypeId() <= 0) {
            return "A valid service type is required";
        }
        if (request.getPurpose() == null || request.getPurpose().isBlank()) {
            return "Request purpose is required";
        }
        if (request.getPurpose().trim().length() > 500) {
            return "Request purpose must not exceed 500 characters";
        }
        if (request.getCreatedByUserId() == null || request.getCreatedByUserId() <= 0) {
            return "A valid staff or administrator account is required";
        }
        if (request.getRemarks() != null && request.getRemarks().trim().length() > 1000) {
            return "Remarks must not exceed 1000 characters";
        }
        request.setPurpose(request.getPurpose().trim());
        request.setRemarks(blankToNull(request.getRemarks()));
        return null;
    }

    private String generateRequestNumber() {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String suffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "REQ-" + date + "-" + suffix;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private boolean canManage(int actingUserId) {
        return authorizationService.canAccessStaffOperations(actingUserId);
    }
}
