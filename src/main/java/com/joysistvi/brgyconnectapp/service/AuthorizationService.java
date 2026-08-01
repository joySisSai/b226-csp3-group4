package com.joysistvi.brgyconnectapp.service;

import com.joysistvi.brgyconnectapp.model.AccountStatus;
import com.joysistvi.brgyconnectapp.model.User;
import com.joysistvi.brgyconnectapp.model.UserRole;
import com.joysistvi.brgyconnectapp.repository.UserRepo;

import java.sql.SQLException;

public class AuthorizationService {
    public static final String STAFF_ACCESS_DENIED =
            "Access denied: an active staff or administrator account is required";
    public static final String ADMIN_ACCESS_DENIED =
            "Access denied: an active administrator account is required";

    private final UserRepo userRepo;

    public AuthorizationService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public boolean canAccessStaffOperations(int userId) {
        User user = getActiveUser(userId);
        return user != null && (user.getRole() == UserRole.STAFF || user.getRole() == UserRole.ADMIN);
    }

    public boolean canAccessAdminOperations(int userId) {
        User user = getActiveUser(userId);
        return user != null && user.getRole() == UserRole.ADMIN;
    }

    public boolean canViewOwnResidentProfile(int userId, int residentId) {
        User user = getActiveUser(userId);
        return user != null &&
                user.getRole() == UserRole.RESIDENT &&
                user.getResidentId() != null &&
                user.getResidentId() == residentId;
    }

    private User getActiveUser(int userId) {
        if (userId <= 0) {
            return null;
        }
        try {
            User user = userRepo.findById(userId).orElse(null);
            return user != null && user.getAccountStatus() == AccountStatus.ACTIVE ? user : null;
        } catch (SQLException exception) {
            return null;
        }
    }
}
