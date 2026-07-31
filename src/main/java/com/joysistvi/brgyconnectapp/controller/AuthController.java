package com.joysistvi.brgyconnectapp.controller;

import com.joysistvi.brgyconnectapp.service.AuthService;
import com.joysistvi.brgyconnectapp.service.LoginResult;

public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public LoginResult login(String username, char[] password) {
        return authService.login(username, password);
    }
}
