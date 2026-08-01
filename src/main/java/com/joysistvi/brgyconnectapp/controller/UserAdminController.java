package com.joysistvi.brgyconnectapp.controller;

import com.joysistvi.brgyconnectapp.model.AccountStatus;
import com.joysistvi.brgyconnectapp.model.User;
import com.joysistvi.brgyconnectapp.model.UserRole;
import com.joysistvi.brgyconnectapp.service.ActivityLogService;
import com.joysistvi.brgyconnectapp.service.UserAdminService;

import java.util.List;

public class UserAdminController {
    private final UserAdminService userAdminService;
    private final ActivityLogService activityLogService;

    public UserAdminController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
        this.activityLogService = null;
    }

    public UserAdminController(UserAdminService userAdminService,
                               ActivityLogService activityLogService) {
        this.userAdminService = userAdminService;
        this.activityLogService = activityLogService;
    }

    public List<User> search(String keyword) {
        return userAdminService.searchUsers(keyword);
    }

    public User getById(int userId) {
        return userAdminService.getUserById(userId);
    }

    public String create(User user, char[] password, int actingAdminId) {
        String result = userAdminService.createUser(user, password);
        if ("User account created successfully".equals(result)) {
            record(actingAdminId, "CREATE", user.getUserId(),
                    "Created user account " + user.getUsername() + " with role " + user.getRole() + ".");
        }
        return result;
    }

    public String changeRole(int targetUserId, UserRole role, int actingAdminId) {
        User target = userAdminService.getUserById(targetUserId);
        String result = userAdminService.changeRole(targetUserId, role, actingAdminId);
        if ("User role updated successfully".equals(result)) {
            String oldRole = target == null || target.getRole() == null ? "UNKNOWN" : target.getRole().name();
            record(actingAdminId, "CHANGE_ROLE", targetUserId,
                    "Changed user " + usernameOrId(target, targetUserId) + " role from " + oldRole + " to " + role + ".");
        }
        return result;
    }

    public String changeStatus(int targetUserId, AccountStatus status, int actingAdminId) {
        User target = userAdminService.getUserById(targetUserId);
        String result = userAdminService.changeStatus(targetUserId, status, actingAdminId);
        if ("Account status updated successfully".equals(result)) {
            String oldStatus = target == null || target.getAccountStatus() == null
                    ? "UNKNOWN"
                    : target.getAccountStatus().name();
            record(actingAdminId, "CHANGE_STATUS", targetUserId,
                    "Changed user " + usernameOrId(target, targetUserId) + " status from " + oldStatus + " to " + status + ".");
        }
        return result;
    }

    public String unlock(int targetUserId, int actingAdminId) {
        User target = userAdminService.getUserById(targetUserId);
        String result = userAdminService.unlockUser(targetUserId);
        if ("User account unlocked successfully".equals(result)) {
            record(actingAdminId, "UNLOCK", targetUserId,
                    "Unlocked user account " + usernameOrId(target, targetUserId) + ".");
        }
        return result;
    }

    public String resetPassword(int targetUserId, char[] password, int actingAdminId) {
        User target = userAdminService.getUserById(targetUserId);
        String result = userAdminService.resetPassword(targetUserId, password);
        if ("Password reset successfully".equals(result)) {
            record(actingAdminId, "RESET_PASSWORD", targetUserId,
                    "Reset password for user account " + usernameOrId(target, targetUserId) + ".");
        }
        return result;
    }

    private void record(int actingAdminId, String action, Integer targetUserId, String description) {
        if (activityLogService != null) {
            activityLogService.record(actingAdminId, action, "USER",
                    targetUserId == null ? null : targetUserId.longValue(), description);
        }
    }

    private String usernameOrId(User user, int userId) {
        return user == null || user.getUsername() == null ? String.valueOf(userId) : user.getUsername();
    }
}
