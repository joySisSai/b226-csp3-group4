package com.joysistvi.brgyconnectapp.service;

import com.joysistvi.brgyconnectapp.model.AccountStatus;
import com.joysistvi.brgyconnectapp.model.User;
import com.joysistvi.brgyconnectapp.repository.UserRepo;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.Optional;

public class AuthService {
    private static final int MAXIMUM_LOGIN_ATTEMPTS = 5;

    private final UserRepo userRepo;

    public AuthService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public LoginResult login(String username, char[] password) {
        if (username == null || username.isBlank() || password == null || password.length == 0) {
            return result(LoginStatus.INVALID_CREDENTIALS, null,
                    "Username and password are required.");
        }

        try {
            Optional<User> possibleUser = userRepo.findByUsername(username.trim());
            if (possibleUser.isEmpty()) {
                return invalidCredentials();
            }

            User user = possibleUser.get();
            LoginResult accountStatusResult = checkAccountStatus(user);
            if (accountStatusResult != null) {
                return accountStatusResult;
            }

            if (!passwordMatches(password, user.getPasswordHash())) {
                userRepo.recordFailedLogin(user.getUserId(), MAXIMUM_LOGIN_ATTEMPTS);

                if (user.getFailedLoginAttempts() + 1 >= MAXIMUM_LOGIN_ATTEMPTS) {
                    return result(LoginStatus.LOCKED, null,
                            "Account locked after too many failed login attempts.");
                }
                return invalidCredentials();
            }

            userRepo.recordSuccessfulLogin(user.getUserId());
            user.setFailedLoginAttempts(0);
            user.setPasswordHash(null);
            return result(LoginStatus.SUCCESS, user, "Login successful.");
        } catch (SQLException exception) {
            return result(LoginStatus.SYSTEM_ERROR, null,
                    "Unable to log in because the database is unavailable.");
        }
    }

    private LoginResult checkAccountStatus(User user) {
        AccountStatus status = user.getAccountStatus();
        return switch (status) {
            case ACTIVE -> null;
            case PENDING_ACTIVATION -> result(LoginStatus.PENDING_ACTIVATION, null,
                    "Your account is awaiting activation.");
            case INACTIVE -> result(LoginStatus.INACTIVE, null,
                    "Your account is inactive.");
            case LOCKED -> result(LoginStatus.LOCKED, null,
                    "Your account is locked. Contact an administrator.");
        };
    }

    private boolean passwordMatches(char[] password, String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            return false;
        }

        try {
            return BCrypt.checkpw(new String(password), passwordHash);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private LoginResult invalidCredentials() {
        return result(LoginStatus.INVALID_CREDENTIALS, null,
                "Invalid username or password.");
    }

    private LoginResult result(LoginStatus status, User user, String message) {
        return new LoginResult(status, user, message);
    }
}
