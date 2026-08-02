package com.joysistvi.brgyconnectapp.repository;

import com.joysistvi.brgyconnectapp.model.ServiceType;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ServiceTypeRepo {
    List<ServiceType> getAll() throws SQLException;

    List<ServiceType> getAllActive() throws SQLException;

    Optional<ServiceType> getById(int serviceTypeId) throws SQLException;

    Optional<ServiceType> getByCode(String serviceCode) throws SQLException;

    boolean save(ServiceType serviceType) throws SQLException;

    boolean update(ServiceType serviceType) throws SQLException;

    boolean setActive(int serviceTypeId, boolean active) throws SQLException;
}
