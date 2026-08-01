package com.joysistvi.brgyconnectapp.repository;

import com.joysistvi.brgyconnectapp.model.ServiceType;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ServiceTypeRepo {
    List<ServiceType> getAllActive() throws SQLException;

    Optional<ServiceType> getById(int serviceTypeId) throws SQLException;
}
