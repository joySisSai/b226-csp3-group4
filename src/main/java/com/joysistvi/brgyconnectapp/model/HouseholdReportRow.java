package com.joysistvi.brgyconnectapp.model;

public record HouseholdReportRow(
        String purok,
        long totalHouseholds,
        long activeHouseholds,
        long inactiveHouseholds,
        long totalMembers
) {
}
