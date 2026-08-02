package com.joysistvi.brgyconnectapp.controller;

import com.joysistvi.brgyconnectapp.model.Resident;
import com.joysistvi.brgyconnectapp.model.User;
import com.joysistvi.brgyconnectapp.service.RegistrationService;

public class RegistrationController {
    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    public String registerWithExistingResident(String residentCode, User user, char[] password) {
        return registrationService.registerWithExistingResident(residentCode, user, password);
    }

    public String registerNewResident(Resident resident, User user, char[] password) {
        return registrationService.registerNewResident(resident, user, password);
    }
}
