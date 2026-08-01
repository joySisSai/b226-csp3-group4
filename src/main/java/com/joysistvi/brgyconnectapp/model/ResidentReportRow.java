package com.joysistvi.brgyconnectapp.model;

public record ResidentReportRow(
        String purok,
        long totalResidents,
        long activeResidents,
        long transferredResidents,
        long deceasedResidents,
        long inactiveResidents,
        long registeredVoters
) {
}
