package com.joysistvi.brgyconnectapp.controller;

import com.joysistvi.brgyconnectapp.model.AccountStatus;
import com.joysistvi.brgyconnectapp.model.User;
import com.joysistvi.brgyconnectapp.model.UserRole;
import com.joysistvi.brgyconnectapp.service.UserAdminService;

import java.util.List;

public class UserAdminController {
    private final UserAdminService userAdminService;

    public UserAdminController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    public List<User> search(String keyword) {
        return userAdminService.searchUsers(keyword);
    }

    public User getById(int userId) {
        return userAdminService.getUserById(userId);
    }

    public String create(User user, char[] password) {
        return userAdminService.createUser(user, password);
    }

    public String changeRole(int targetUserId, UserRole role, int actingAdminId) {
        return userAdminService.changeRole(targetUserId, role, actingAdminId);
    }

    public String changeStatus(int targetUserId, AccountStatus status, int actingAdminId) {
        return userAdminService.changeStatus(targetUserId, status, actingAdminId);
    }

    public String unlock(int targetUserId) {
        return userAdminService.unlockUser(targetUserId);
    }

    public String resetPassword(int targetUserId, char[] password) {
        return userAdminService.resetPassword(targetUserId, password);
    }
}
