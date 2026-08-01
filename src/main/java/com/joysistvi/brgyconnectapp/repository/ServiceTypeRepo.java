package com.joysistvi.brgyconnectapp.repository;

import com.joysistvi.brgyconnectapp.model.ServiceType;
import java.util.List;
import java.util.Optional;

public interface ServiceTypeRepo {
    List<ServiceType> getAllActive();
    List<ServiceType> getAll();
    Optional<ServiceType> getById(Integer id);
    Optional<ServiceType> getByCode(String code);
    Optional<ServiceType> getByName(String name);
    boolean save(ServiceType type);
    boolean update(ServiceType type);
    boolean deactivate(Integer id);
}