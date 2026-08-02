package com.joysistvi.brgyconnectapp.service;

import com.joysistvi.brgyconnectapp.model.AccountStatus;
import com.joysistvi.brgyconnectapp.model.User;
import com.joysistvi.brgyconnectapp.model.UserRole;
import com.joysistvi.brgyconnectapp.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class AuthorizationServiceTest {
    private UserRepo userRepo;
    private AuthorizationService authService;

    @BeforeEach
    public void setUp() {
        userRepo = Mockito.mock(UserRepo.class);
        authService = new AuthorizationService(userRepo);
    }

    @Test
    public void testCanAccessAdminOperations_AdminActive() throws SQLException {
        User admin = new User();
        admin.setRole(UserRole.ADMIN);
        admin.setAccountStatus(AccountStatus.ACTIVE);
        when(userRepo.findById(1)).thenReturn(Optional.of(admin));

        assertTrue(authService.canAccessAdminOperations(1));
    }

    @Test
    public void testCanAccessAdminOperations_StaffActive() throws SQLException {
        User staff = new User();
        staff.setRole(UserRole.STAFF);
        staff.setAccountStatus(AccountStatus.ACTIVE);
        when(userRepo.findById(1)).thenReturn(Optional.of(staff));

        assertFalse(authService.canAccessAdminOperations(1));
    }

    @Test
    public void testCanAccessStaffOperations_StaffActive() throws SQLException {
        User staff = new User();
        staff.setRole(UserRole.STAFF);
        staff.setAccountStatus(AccountStatus.ACTIVE);
        when(userRepo.findById(1)).thenReturn(Optional.of(staff));

        assertTrue(authService.canAccessStaffOperations(1));
    }

    @Test
    public void testCanViewOwnResidentProfile_ValidResident() throws SQLException {
        User resident = new User();
        resident.setRole(UserRole.RESIDENT);
        resident.setAccountStatus(AccountStatus.ACTIVE);
        resident.setResidentId(10);
        when(userRepo.findById(1)).thenReturn(Optional.of(resident));

        assertTrue(authService.canViewOwnResidentProfile(1, 10));
    }

    @Test
    public void testCanViewOwnResidentProfile_InvalidResidentId() throws SQLException {
        User resident = new User();
        resident.setRole(UserRole.RESIDENT);
        resident.setAccountStatus(AccountStatus.ACTIVE);
        resident.setResidentId(10);
        when(userRepo.findById(1)).thenReturn(Optional.of(resident));

        assertFalse(authService.canViewOwnResidentProfile(1, 20)); // Mismatched ID
    }

    @Test
    public void testInactiveUser_Denied() throws SQLException {
        User admin = new User();
        admin.setRole(UserRole.ADMIN);
        admin.setAccountStatus(AccountStatus.INACTIVE);
        when(userRepo.findById(1)).thenReturn(Optional.of(admin));

        assertFalse(authService.canAccessAdminOperations(1));
    }
}
