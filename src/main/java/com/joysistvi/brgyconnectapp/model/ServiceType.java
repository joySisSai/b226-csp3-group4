package com.joysistvi.brgyconnectapp.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ServiceType {
    private Integer serviceTypeId;
    private String serviceCode;
    private String serviceName;
    private String description;
    private BigDecimal defaultFee;
    private int expectedProcessingDays;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ServiceType() {
    }

    public Integer getServiceTypeId() { return serviceTypeId; }
    public void setServiceTypeId(Integer serviceTypeId) { this.serviceTypeId = serviceTypeId; }
    public String getServiceCode() { return serviceCode; }
    public void setServiceCode(String serviceCode) { this.serviceCode = serviceCode; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getDefaultFee() { return defaultFee; }
    public void setDefaultFee(BigDecimal defaultFee) { this.defaultFee = defaultFee; }
    public int getExpectedProcessingDays() { return expectedProcessingDays; }
    public void setExpectedProcessingDays(int expectedProcessingDays) { this.expectedProcessingDays = expectedProcessingDays; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
