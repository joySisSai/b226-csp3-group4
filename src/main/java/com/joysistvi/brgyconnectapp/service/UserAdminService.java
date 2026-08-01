package com.joysistvi.brgyconnectapp.service;

import com.joysistvi.brgyconnectapp.model.AccountStatus;
import com.joysistvi.brgyconnectapp.model.Resident;
import com.joysistvi.brgyconnectapp.model.ResidencyStatus;
import com.joysistvi.brgyconnectapp.model.User;
import com.joysistvi.brgyconnectapp.model.UserRole;
import com.joysistvi.brgyconnectapp.repository.ResidentRepo;
import com.joysistvi.brgyconnectapp.repository.UserRepo;
import org.mindrot.jbcrypt.BCrypt;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class UserAdminService {
    private static final int MAXIMUM_RESULTS = 100;
    private static final Pattern USERNAME_PATTERN = Pattern.compile("[A-Za-z0-9._-]{4,50}");
    private static final String DATABASE_ERROR =
            "Unable to complete the operation because the database is unavailable";

    private final UserRepo userRepo;
    private final ResidentRepo residentRepo;
    private final AuthorizationService authorizationService;

    public UserAdminService(UserRepo userRepo,
                            ResidentRepo residentRepo,
                            AuthorizationService authorizationService) {
        this.userRepo = userRepo;
        this.residentRepo = residentRepo;
        this.authorizationService = authorizationService;
    }

    public List<User> getAllUsers(int actingAdminId) {
        if (!canManage(actingAdminId)) {
            return List.of();
        }
        try {
            return userRepo.getAll();
        } catch (SQLException exception) {
            return List.of();
        }
    }

    public List<User> searchUsers(String keyword, int actingAdminId) {
        if (!canManage(actingAdminId)) {
            return List.of();
        }
        if (keyword == null || keyword.isBlank()) {
            return getAllUsers(actingAdminId);
        }
        try {
            return userRepo.search(keyword.trim(), MAXIMUM_RESULTS);
        } catch (SQLException exception) {
            return List.of();
        }
    }

    public User getUserById(int userId, int actingAdminId) {
        if (!canManage(actingAdminId)) {
            return null;
        }
        if (userId <= 0) {
            return null;
        }
        try {
            return userRepo.findById(userId).orElse(null);
        } catch (SQLException exception) {
            return null;
        }
    }

    public String createUser(User user, char[] password, int actingAdminId) {
        if (!canManage(actingAdminId)) {
            clearPassword(password);
            return AuthorizationService.ADMIN_ACCESS_DENIED;
        }
        try {
            String validationError = validateNewUser(user, password);
            if (validationError != null) {
                return validationError;
            }

            user.setUsername(user.getUsername().trim().toLowerCase());
            user.setDisplayName(user.getDisplayName().trim());
            user.setAccountStatus(AccountStatus.ACTIVE);
            if (userRepo.findByUsername(user.getUsername()).isPresent()) {
                return "Username already exists";
            }

            if (user.getRole() == UserRole.RESIDENT) {
                Resident resident = residentRepo.getById(user.getResidentId()).orElse(null);
                if (resident == null) {
                    return "Resident record not found";
                }
                if (resident.getResidencyStatus() != ResidencyStatus.ACTIVE) {
                    return "Only active residents can receive resident accounts";
                }
                if (userRepo.findByResidentId(user.getResidentId()).isPresent()) {
                    return "This resident already has a user account";
                }
            } else {
                user.setResidentId(null);
            }

            user.setPasswordHash(hashPassword(password));
            boolean saved = userRepo.save(user);
            user.setPasswordHash(null);
            return saved ? "User account created successfully" : "Failed to create user account";
        } catch (SQLException exception) {
            return DATABASE_ERROR;
        } finally {
            clearPassword(password);
        }
    }

    public String changeRole(int targetUserId, UserRole newRole, int actingAdminId) {
        if (!canManage(actingAdminId)) {
            return AuthorizationService.ADMIN_ACCESS_DENIED;
        }
        if (targetUserId <= 0 || newRole == null) {
            return "A valid user and role are required";
        }
        try {
            User target = userRepo.findById(targetUserId).orElse(null);
            if (target == null) {
                return "User account not found";
            }
            if (target.getRole() == newRole) {
                return "User already has the selected role";
            }
            if (targetUserId == actingAdminId && newRole != UserRole.ADMIN) {
                return "You cannot remove your own administrator role";
            }
            if (newRole == UserRole.RESIDENT && target.getResidentId() == null) {
                return "An account without a linked resident record cannot become a resident account";
            }
            if (target.getRole() == UserRole.ADMIN && target.getAccountStatus() == AccountStatus.ACTIVE &&
                    newRole != UserRole.ADMIN && userRepo.countActiveAdmins() <= 1) {
                return "The last active administrator cannot be demoted";
            }

            return userRepo.updateRole(targetUserId, newRole)
                    ? "User role updated successfully"
                    : "Failed to update user role";
        } catch (SQLException exception) {
            return DATABASE_ERROR;
        }
    }

    public String changeStatus(int targetUserId, AccountStatus newStatus, int actingAdminId) {
        if (!canManage(actingAdminId)) {
            return AuthorizationService.ADMIN_ACCESS_DENIED;
        }
        if (targetUserId <= 0 || newStatus == null || newStatus == AccountStatus.LOCKED) {
            return "Choose a valid account status";
        }
        try {
            User target = userRepo.findById(targetUserId).orElse(null);
            if (target == null) {
                return "User account not found";
            }
            if (target.getAccountStatus() == newStatus) {
                return "User account already has the selected status";
            }
            if (targetUserId == actingAdminId && newStatus != AccountStatus.ACTIVE) {
                return "You cannot deactivate your own administrator account";
            }
            if (target.getRole() == UserRole.ADMIN && target.getAccountStatus() == AccountStatus.ACTIVE &&
                    newStatus != AccountStatus.ACTIVE && userRepo.countActiveAdmins() <= 1) {
                return "The last active administrator cannot be deactivated";
            }

            return userRepo.updateStatus(targetUserId, newStatus)
                    ? "Account status updated successfully"
                    : "Failed to update account status";
        } catch (SQLException exception) {
            return DATABASE_ERROR;
        }
    }

    public String unlockUser(int targetUserId, int actingAdminId) {
        if (!canManage(actingAdminId)) {
            return AuthorizationService.ADMIN_ACCESS_DENIED;
        }
        if (targetUserId <= 0) {
            return "Invalid user ID";
        }
        try {
            User target = userRepo.findById(targetUserId).orElse(null);
            if (target == null) {
                return "User account not found";
            }
            if (target.getAccountStatus() != AccountStatus.LOCKED) {
                return "Only locked accounts can be unlocked";
            }
            return userRepo.unlock(targetUserId)
                    ? "User account unlocked successfully"
                    : "Failed to unlock user account";
        } catch (SQLException exception) {
            return DATABASE_ERROR;
        }
    }

    public String resetPassword(int targetUserId, char[] newPassword, int actingAdminId) {
        if (!canManage(actingAdminId)) {
            clearPassword(newPassword);
            return AuthorizationService.ADMIN_ACCESS_DENIED;
        }
        try {
            if (targetUserId <= 0) {
                return "Invalid user ID";
            }
            String passwordError = validatePassword(newPassword);
            if (passwordError != null) {
                return passwordError;
            }
            if (userRepo.findById(targetUserId).isEmpty()) {
                return "User account not found";
            }

            return userRepo.updatePassword(targetUserId, hashPassword(newPassword))
                    ? "Password reset successfully"
                    : "Failed to reset password";
        } catch (SQLException exception) {
            return DATABASE_ERROR;
        } finally {
            clearPassword(newPassword);
        }
    }

    private String validateNewUser(User user, char[] password) {
        if (user == null) {
            return "User account information is required";
        }
        if (user.getUsername() == null || !USERNAME_PATTERN.matcher(user.getUsername().trim()).matches()) {
            return "Username must be 4-50 characters using letters, numbers, dots, underscores, or hyphens";
        }
        if (user.getDisplayName() == null || user.getDisplayName().isBlank() ||
                user.getDisplayName().trim().length() > 150) {
            return "Display name is required and must not exceed 150 characters";
        }
        if (user.getRole() == null) {
            return "User role is required";
        }
        if (user.getRole() == UserRole.RESIDENT &&
                (user.getResidentId() == null || user.getResidentId() <= 0)) {
            return "A resident account must be linked to a valid resident ID";
        }
        return validatePassword(password);
    }

    private String validatePassword(char[] password) {
        if (password == null || password.length < 8) {
            return "Password must contain at least 8 characters";
        }
        if (new String(password).getBytes(StandardCharsets.UTF_8).length > 72) {
            return "Password must not exceed 72 UTF-8 bytes";
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

    public boolean canManage(int actingAdminId) {
        return authorizationService.canAccessAdminOperations(actingAdminId);
    }
}
