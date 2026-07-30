package com.barangayconnect.model.records;

import com.barangayconnect.model.enums.CivilStatus;
import com.barangayconnect.model.enums.ResidencyStatus;
import com.barangayconnect.model.enums.Sex;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record Resident(
        Integer id,
        String code,
        Integer householdId,
        String firstName,
        String middleName,
        String lastName,
        String suffix,
        LocalDate birthDate,
        Sex sex,
        CivilStatus civilStatus,
        String contactNumber,
        String email,
        String occupation,
        boolean registeredVoter,
        boolean householdHead,
        ResidencyStatus status,
        LocalDate dateRegistered,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
