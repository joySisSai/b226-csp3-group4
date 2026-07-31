package com.joysistvi.brgyconnectapp.service;

import com.joysistvi.brgyconnectapp.model.User;

public record LoginResult(LoginStatus status, User user, String message) {
    public boolean isSuccessful() {
        return status == LoginStatus.SUCCESS;
    }
}
