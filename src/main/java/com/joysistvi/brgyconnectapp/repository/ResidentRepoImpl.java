package com.joysistvi.brgyconnectapp.repository;

import com.joysistvi.brgyconnectapp.config.ConnectionFactory;
import com.joysistvi.brgyconnectapp.config.DbConnection;
import com.joysistvi.brgyconnectapp.model.Resident;
import com.joysistvi.brgyconnectapp.model.Sex;
import com.joysistvi.brgyconnectapp.model.CivilStatus;
import com.joysistvi.brgyconnectapp.model.ResidencyStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Database implementation of ResidentRepo.
 * Uses project-standard ConnectionFactory and enum conversion.
 */
public class ResidentRepoImpl implements ResidentRepo {

    private final ConnectionFactory dbFactory = new DbConnection();

    // SQL queries match the exact column names from barangayconnect_db.residents
    private static final String SELECT_ALL = "SELECT * FROM residents ORDER BY last_name, first_name";
    private static final String SELECT_BY_ID = "SELECT * FROM residents WHERE resident_id = ?";
    private static final String INSERT = """
        INSERT INTO residents (
            resident_code, household_id, first_name, middle_name, last_name, suffix,
            birth_date, sex, civil_status, contact_number, email, occupation,
            is_registered_voter, is_household_head, residency_status, date_registered
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
    private static final String UPDATE = """
        UPDATE residents SET
            resident_code=?, household_id=?, first_name=?, middle_name=?, last_name=?, suffix=?,
            birth_date=?, sex=?, civil_status=?, contact_number=?, email=?, occupation=?,
            is_registered_voter=?, is_household_head=?, residency_status=?
        WHERE resident_id=?
        """;
    private static final String DELETE = "DELETE FROM residents WHERE resident_id = ?";

    @Override
    public List<Resident> getAllResidents() {
        List<Resident> list = new ArrayList<>();
        try (Connection conn = dbFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapToResident(rs));
            }
        } catch (SQLException e) {
            System.err.println("Failed to fetch residents: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Resident getById(int id) {
        try (Connection conn = dbFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapToResident(rs) : null;
            }
        } catch (SQLException e) {
            System.err.println("Failed to fetch resident: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean create(Resident resident) {
        try (Connection conn = dbFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT)) {
            setStatementParams(stmt, resident);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Failed to add resident: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean update(Resident resident) {
        try (Connection conn = dbFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE)) {
            setStatementParams(stmt, resident);
            stmt.setInt(16, resident.getResidentId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Failed to update resident: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        try (Connection conn = dbFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Failed to delete resident: " + e.getMessage());
        }
        return false;
    }

    /**
     * Converts database ResultSet to Resident object.
     * Converts string values from DB to project enums using valueOf().
     */
    private Resident mapToResident(ResultSet rs) throws SQLException {
        Resident r = new Resident();
        r.setResidentId(rs.getInt("resident_id"));
        r.setResidentCode(rs.getString("resident_code"));
        r.setHouseholdId(rs.getObject("household_id", Integer.class));
        r.setFirstName(rs.getString("first_name"));
        r.setMiddleName(rs.getString("middle_name"));
        r.setLastName(rs.getString("last_name"));
        r.setSuffix(rs.getString("suffix"));
        r.setBirthDate(rs.getDate("birth_date").toLocalDate());
        // Convert DB string to Enum
        r.setSex(Sex.valueOf(rs.getString("sex")));
        r.setCivilStatus(CivilStatus.valueOf(rs.getString("civil_status")));
        r.setContactNumber(rs.getString("contact_number"));
        r.setEmail(rs.getString("email"));
        r.setOccupation(rs.getString("occupation"));
        r.setRegisteredVoter(rs.getBoolean("is_registered_voter"));
        r.setHouseholdHead(rs.getBoolean("is_household_head"));
        r.setResidencyStatus(ResidencyStatus.valueOf(rs.getString("residency_status")));
        r.setDateRegistered(rs.getDate("date_registered").toLocalDate());
        return r;
    }

    /**
     * Sets values for PreparedStatement.
     * Converts Enum back to database string using .name().
     */
    private void setStatementParams(PreparedStatement stmt, Resident r) throws SQLException {
        stmt.setString(1, r.getResidentCode());
        stmt.setObject(2, r.getHouseholdId());
        stmt.setString(3, r.getFirstName());
        stmt.setString(4, r.getMiddleName());
        stmt.setString(5, r.getLastName());
        stmt.setString(6, r.getSuffix());
        stmt.setDate(7, Date.valueOf(r.getBirthDate()));
        // Convert Enum to string for DB storage
        stmt.setString(8, r.getSex().name());
        stmt.setString(9, r.getCivilStatus().name());
        stmt.setString(10, r.getContactNumber());
        stmt.setString(11, r.getEmail());
        stmt.setString(12, r.getOccupation());
        stmt.setBoolean(13, r.isRegisteredVoter());
        stmt.setBoolean(14, r.isHouseholdHead());
        stmt.setString(15, r.getResidencyStatus().name());
    }
}