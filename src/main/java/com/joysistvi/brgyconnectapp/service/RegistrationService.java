package com.joysistvi.brgyconnectapp.service;

import com.joysistvi.brgyconnectapp.model.AccountStatus;
import com.joysistvi.brgyconnectapp.model.Resident;
import com.joysistvi.brgyconnectapp.model.ResidencyStatus;
import com.joysistvi.brgyconnectapp.model.User;
import com.joysistvi.brgyconnectapp.model.UserRole;
import com.joysistvi.brgyconnectapp.repository.ResidentRepo;
import com.joysistvi.brgyconnectapp.repository.UserRepo;
import com.joysistvi.brgyconnectapp.validation.ResidentFieldValidator;
import org.mindrot.jbcrypt.BCrypt;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.regex.Pattern;

public class RegistrationService {
    private static final Pattern USERNAME_PATTERN = Pattern.compile("[A-Za-z0-9._-]{4,50}");
    private final UserRepo userRepo;
    private final ResidentRepo residentRepo;

    public RegistrationService(UserRepo userRepo, ResidentRepo residentRepo) {
        this.userRepo = userRepo;
        this.residentRepo = residentRepo;
    }

    public String registerWithExistingResident(String residentCode, User user, char[] password) {
        try {
            if (residentCode == null || residentCode.isBlank()) {
                return "Resident code is required.";
            }

            Resident resident = residentRepo.getByCode(residentCode.trim()).orElse(null);
            if (resident == null) {
                return "No resident found with the provided code.";
            }

            if (resident.getResidencyStatus() != ResidencyStatus.ACTIVE) {
                return "Only active residents can register an account.";
            }

            if (userRepo.findByResidentId(resident.getResidentId()).isPresent()) {
                return "This resident already has an account registered.";
            }

            user.setResidentId(resident.getResidentId());
            return processUserRegistration(user, password);
        } catch (SQLException e) {
            return "A database error occurred during registration.";
        } finally {
            clearPassword(password);
        }
    }

    public String registerNewResident(Resident resident, User user, char[] password) {
        try {
            String residentValidationError = validateResident(resident);
            if (residentValidationError != null) {
                return residentValidationError;
            }

            if (!residentRepo.save(resident)) {
                return "Failed to save the resident details.";
            }

            user.setResidentId(resident.getResidentId());
            String userError = processUserRegistration(user, password);
            if (!userError.toLowerCase().contains("success")) {
                // Warning: The resident was created but user registration failed.
                return userError + " (Note: Resident profile was still created with code " + resident.getResidentCode() + ")";
            }

            return "Registration successful. Please wait for staff activation. Your resident code is " + resident.getResidentCode();
        } catch (SQLException e) {
            if (DatabaseErrors.isConstraintViolation(e)) {
                return "A resident or username conflict occurred.";
            }
            return "A database error occurred during registration.";
        } finally {
            clearPassword(password);
        }
    }

    private String processUserRegistration(User user, char[] password) throws SQLException {
        String validationError = validateNewUser(user, password);
        if (validationError != null) {
            return validationError;
        }

        user.setUsername(user.getUsername().trim().toLowerCase());
        user.setDisplayName(user.getDisplayName().trim());
        user.setRole(UserRole.RESIDENT);
        user.setAccountStatus(AccountStatus.PENDING_ACTIVATION);

        if (userRepo.findByUsername(user.getUsername()).isPresent()) {
            return "Username already exists.";
        }

        user.setPasswordHash(hashPassword(password));
        boolean saved = userRepo.save(user);
        user.setPasswordHash(null);

        return saved ? "Registration successful. Please wait for staff activation." : "Failed to create user account.";
    }

    private String validateNewUser(User user, char[] password) {
        if (user == null) {
            return "User account information is required.";
        }
        if (user.getUsername() == null || !USERNAME_PATTERN.matcher(user.getUsername().trim()).matches()) {
            return "Username must be 4-50 characters using letters, numbers, dots, underscores, or hyphens.";
        }
        if (user.getDisplayName() == null || user.getDisplayName().isBlank() ||
                user.getDisplayName().trim().length() > 150) {
            return "Display name is required and must not exceed 150 characters.";
        }
        return validatePassword(password);
    }

    private String validatePassword(char[] password) {
        if (password == null || password.length < 8) {
            return "Password must contain at least 8 characters.";
        }
        if (new String(password).getBytes(StandardCharsets.UTF_8).length > 72) {
            return "Password must not exceed 72 UTF-8 bytes.";
        }
        return null;
    }

    private String validateResident(Resident resident) {
        if (resident == null) {
            return "Resident information is required.";
        }
        if (resident.getFirstName() == null || resident.getFirstName().isBlank() ||
                resident.getLastName() == null || resident.getLastName().isBlank()) {
            return "First name and last name are required.";
        }
        if (resident.getSex() == null || resident.getCivilStatus() == null || resident.getResidencyStatus() == null) {
            return "Sex, civil status, and residency status are required.";
        }
        String birthDateError = ResidentFieldValidator.validateBirthDate(resident.getBirthDate());
        if (birthDateError != null) return birthDateError;

        resident.setContactNumber(ResidentFieldValidator.normalizeOptional(resident.getContactNumber()));
        String contactNumberError = ResidentFieldValidator.validateContactNumber(resident.getContactNumber());
        if (contactNumberError != null) return contactNumberError;

        resident.setEmail(ResidentFieldValidator.normalizeOptional(resident.getEmail()));
        String emailError = ResidentFieldValidator.validateEmail(resident.getEmail());
        if (emailError != null) return emailError;

        if (resident.getHouseholdId() != null && resident.getHouseholdId() <= 0) {
            return "Household ID must be a positive whole number.";
        }
        return null;
    }

    private String hashPassword(char[] password) {
        return BCrypt.hashpw(new String(password), BCrypt.gensalt(12));
    }

    private void clearPassword(char[] password) {
        if (password != null) {
            Arrays.fill(password, '\0');
        }
    }
}
