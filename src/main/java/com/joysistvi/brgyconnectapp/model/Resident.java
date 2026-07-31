package com.joysistvi.brgyconnectapp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Resident {
    private Integer residentId;
    private String residentCode;
    private Integer householdId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String suffix;
    private LocalDate birthDate;
    private Sex sex;
    private CivilStatus civilStatus;
    private String contactNumber;
    private String email;
    private String occupation;
    private boolean registeredVoter;
    private boolean householdHead;
    private ResidencyStatus residencyStatus;
    private LocalDate dateRegistered;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Resident() {
    }

    public Integer getResidentId() { return residentId; }
    public void setResidentId(Integer residentId) { this.residentId = residentId; }
    public String getResidentCode() { return residentCode; }
    public void setResidentCode(String residentCode) { this.residentCode = residentCode; }
    public Integer getHouseholdId() { return householdId; }
    public void setHouseholdId(Integer householdId) { this.householdId = householdId; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getSuffix() { return suffix; }
    public void setSuffix(String suffix) { this.suffix = suffix; }
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public Sex getSex() { return sex; }
    public void setSex(Sex sex) { this.sex = sex; }
    public CivilStatus getCivilStatus() { return civilStatus; }
    public void setCivilStatus(CivilStatus civilStatus) { this.civilStatus = civilStatus; }
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getOccupation() { return occupation; }
    public void setOccupation(String occupation) { this.occupation = occupation; }
    public boolean isRegisteredVoter() { return registeredVoter; }
    public void setRegisteredVoter(boolean registeredVoter) { this.registeredVoter = registeredVoter; }
    public boolean isHouseholdHead() { return householdHead; }
    public void setHouseholdHead(boolean householdHead) { this.householdHead = householdHead; }
    public ResidencyStatus getResidencyStatus() { return residencyStatus; }
    public void setResidencyStatus(ResidencyStatus residencyStatus) { this.residencyStatus = residencyStatus; }
    public LocalDate getDateRegistered() { return dateRegistered; }
    public void setDateRegistered(LocalDate dateRegistered) { this.dateRegistered = dateRegistered; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
 