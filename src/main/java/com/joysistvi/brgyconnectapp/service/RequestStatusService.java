package com.joysistvi.brgyconnectapp.service;

import java.util.List;
import com.joysistvi.brgyconnectapp.model.RequestStatus;
import com.joysistvi.brgyconnectapp.model.ServiceRequest;
import com.joysistvi.brgyconnectapp.repository.RequestStatusRepo;
import com.joysistvi.brgyconnectapp.repository.RequestStatusRepoImpl;
import com.joysistvi.brgyconnectapp.config.DbConnection;
import com.joysistvi.brgyconnectapp.model.RequestStatusHistory;
import com.joysistvi.brgyconnectapp.repository.RequestStatusHistoryRepo;
import com.joysistvi.brgyconnectapp.repository.RequestStatusHistoryRepoImpl;

public class RequestStatusService{

    private final RequestStatusRepo requestStatusRepo;
    private final RequestStatusHistoryRepo historyRepo;

    public RequestStatusService() {
        requestStatusRepo = new RequestStatusRepoImpl();
        historyRepo = new RequestStatusHistoryRepoImpl(new DbConnection());
    }
    public ServiceRequest getRequestById(Long requestId) {
        return requestStatusRepo.findById(requestId);
    }
    public List<RequestStatusHistory> getRequestHistory(Long requestId) {
        return historyRepo.findByRequestId(requestId);
    }

    public boolean updateRequestStatus(Long requestId,
                                       RequestStatus newStatus,
                                       Integer processedByUserId,
                                       String remarks) {

        ServiceRequest request = requestStatusRepo.findById(requestId);

        if (request == null) {
            throw new RuntimeException("Request not found.");
        }

        System.out.println("Current Status: " + request.getStatus());

        RequestStatus currentStatus = request.getStatus();

        if (!isValidTransition(currentStatus, newStatus)) {
            throw new IllegalArgumentException(
                    "Invalid status transition from "
                            + currentStatus + " to " + newStatus);
        }


        boolean updated = requestStatusRepo.updateStatus(
                requestId,
                newStatus,
                processedByUserId,
                remarks
        );

        if (updated) {

            RequestStatusHistory history = new RequestStatusHistory();

            history.setRequestId(requestId);
            history.setOldStatus(currentStatus);
            history.setNewStatus(newStatus);
            history.setRemarks(remarks);
            history.setChangedByUserId(processedByUserId);

            historyRepo.save(history);
        }

        return updated;
    }

    private boolean isValidTransition(RequestStatus current, RequestStatus next) {

        switch (current) {
            case PENDING:
                return next == RequestStatus.UNDER_REVIEW
                        || next == RequestStatus.CANCELLED;

            case UNDER_REVIEW:
                return next == RequestStatus.APPROVED
                        || next == RequestStatus.REJECTED
                        || next == RequestStatus.CANCELLED;

            case APPROVED:
                return next == RequestStatus.RELEASED;

            case RELEASED:
            case REJECTED:
            case CANCELLED:
                return false;

            default:
                return false;
        }
    }
    public void viewRequestHistory(Long requestId) {

        List<RequestStatusHistory> histories =
                historyRepo.findByRequestId(requestId);

        if (histories.isEmpty()) {
            System.out.println("No history found.");
            return;
        }

        System.out.println("==============================================================");
        System.out.println("                 REQUEST STATUS HISTORY");
        System.out.println("==============================================================");

        for (RequestStatusHistory history : histories) {

            System.out.println("History ID : " + history.getHistoryId());
            System.out.println("Old Status : " + history.getOldStatus());
            System.out.println("New Status : " + history.getNewStatus());
            System.out.println("Remarks    : " + history.getRemarks());
            System.out.println("Changed By : " + history.getChangedByUserId());
            System.out.println("Changed At : " + history.getChangedAt());
            System.out.println("--------------------------------------------------------------");
        }
    }
}