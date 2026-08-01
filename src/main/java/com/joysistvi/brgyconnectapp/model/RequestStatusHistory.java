package com.joysistvi.brgyconnectapp.model;

import java.time.LocalDateTime;

public class    RequestStatusHistory {
    private Long historyId;
    private Long requestId;
    private RequestStatus oldStatus;
    private RequestStatus newStatus;
    private String remarks;
    private Integer changedByUserId;
    private LocalDateTime changedAt;

    public RequestStatusHistory() {
    }

    public RequestStatusHistory(Long historyId, Long requestId, RequestStatus oldStatus,
                                RequestStatus newStatus, String remarks, Integer changedByUserId,
                                LocalDateTime changedAt) {
        this.historyId = historyId;
        this.requestId = requestId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.remarks = remarks;
        this.changedByUserId = changedByUserId;
        this.changedAt = changedAt;
    }

    public Long getHistoryId() { return historyId; }
    public void setHistoryId(Long historyId) { this.historyId = historyId; }
    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }
    public RequestStatus getOldStatus() { return oldStatus; }
    public void setOldStatus(RequestStatus oldStatus) { this.oldStatus = oldStatus; }
    public RequestStatus getNewStatus() { return newStatus; }
    public void setNewStatus(RequestStatus newStatus) { this.newStatus = newStatus; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public Integer getChangedByUserId() { return changedByUserId; }
    public void setChangedByUserId(Integer changedByUserId) { this.changedByUserId = changedByUserId; }
    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }
}
