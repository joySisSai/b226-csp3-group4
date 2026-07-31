package com.joysistvi.brgyconnectapp.model;

import java.time.LocalDateTime;

public class Household {
    private Integer householdId;
    private String householdCode;
    private String addressLine;
    private String purok;
    private HouseholdStatus householdStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Household() {
    }

    public Household(Integer householdId, String householdCode, String addressLine, String purok,
                     HouseholdStatus householdStatus, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.householdId = householdId;
        this.householdCode = householdCode;
        this.addressLine = addressLine;
        this.purok = purok;
        this.householdStatus = householdStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Integer getHouseholdId() { return householdId; }
    public void setHouseholdId(Integer householdId) { this.householdId = householdId; }
    public String getHouseholdCode() { return householdCode; }
    public void setHouseholdCode(String householdCode) { this.householdCode = householdCode; }
    public String getAddressLine() { return addressLine; }
    public void setAddressLine(String addressLine) { this.addressLine = addressLine; }
    public String getPurok() { return purok; }
    public void setPurok(String purok) { this.purok = purok; }
    public HouseholdStatus getHouseholdStatus() { return householdStatus; }
    public void setHouseholdStatus(HouseholdStatus householdStatus) { this.householdStatus = householdStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
