package com.joysistvi.brgyconnectapp.repository;

import com.joysistvi.brgyconnectapp.model.Household;
import com.joysistvi.brgyconnectapp.model.Resident;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface HouseholdRepo {
    List<Household> getAll() throws SQLException;

    Optional<Household> getById(int householdId) throws SQLException;

    Optional<Household> getByCode(String householdCode) throws SQLException;

    List<Household> search(String keyword) throws SQLException;

    boolean save(Household household) throws SQLException;

    boolean update(Household household) throws SQLException;

    boolean deactivate(int householdId) throws SQLException;

    List<Resident> getMembers(int householdId) throws SQLException;

    boolean addMember(int householdId, int residentId) throws SQLException;

    boolean removeMember(int householdId, int residentId) throws SQLException;

    boolean assignHouseholdHead(int householdId, int residentId) throws SQLException;
}
