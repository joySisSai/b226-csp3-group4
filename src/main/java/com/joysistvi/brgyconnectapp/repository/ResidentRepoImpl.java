package com.joysistvi.brgyconnectapp.repository;

import com.joysistvi.brgyconnectapp.config.ConnectionFactory;
import com.joysistvi.brgyconnectapp.config.DbConnection;
import com.joysistvi.brgyconnectapp.model.CivilStatus;
import com.joysistvi.brgyconnectapp.model.Resident;
import com.joysistvi.brgyconnectapp.model.ResidencyStatus;
import com.joysistvi.brgyconnectapp.model.Sex;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ResidentRepoImpl implements ResidentRepo {
    private final ConnectionFactory dbFactory = new DbConnection();

    // Get all residents with proper error logging
    @Override
    public List<Resident> getAll() {
        List<Resident> list = new ArrayList<>();
        String sql = "SELECT * FROM residents ORDER BY last_name, first_name";
        try (Connection conn = dbFactory.openConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapToResident(rs));
        } catch (SQLException e) {
            System.err.println("Error fetching residents: " + e.getMessage());
        }
        return list;
    }

    // Get one resident using their primary key ID
    @Override
    public Optional<Resident> getById(int id) {
        try (Connection conn = dbFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM residents WHERE resident_id = ?")) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapToResident(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            System.err.println("Error fetching resident by ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    // Get one resident using their unique resident code
    @Override
    public Optional<Resident> getByCode(String code) {
        try (Connection conn = dbFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM residents WHERE resident_code = ?")) {
            stmt.setString(1, code);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapToResident(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            System.err.println("Error fetching resident by code: " + e.getMessage());
        }
        return Optional.empty();
    }

    // Search for residents — matches first name, last name, or resident code
    @Override
    public List<Resident> searchByNameOrCode(String keyword) {
        List<Resident> list = new ArrayList<>();
        String sql = """
            SELECT * FROM residents 
            WHERE first_name LIKE ? 
            OR last_name LIKE ? 
            OR resident_code LIKE ?
            ORDER BY last_name, first_name
            """;
        try (Connection conn = dbFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%"; // % matches any characters before/after the keyword
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapToResident(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error searching residents: " + e.getMessage());
        }
        return list;
    }

    // Save new resident — first checks if the code already exists to avoid duplicates
    @Override
    public boolean save(Resident resident) {
        if (getByCode(resident.getResidentCode()).isPresent()) return false;
        String sql = """
            INSERT INTO residents (
                resident_code, household_id, suffix, first_name, middle_name, last_name,
                birth_date, sex, civil_status, residency_status, email, occupation,
                is_registered_voter, is_household_head, contact_number, date_registered
            ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?, NOW())
            """;
        try (Connection conn = dbFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, resident.getResidentCode());
            stmt.setObject(2, resident.getHouseholdId());
            stmt.setString(3, resident.getSuffix());
            stmt.setString(4, resident.getFirstName());
            stmt.setString(5, resident.getMiddleName());
            stmt.setString(6, resident.getLastName());
            stmt.setDate(7, Date.valueOf(resident.getBirthDate()));
            stmt.setString(8, resident.getSex().name());
            stmt.setString(9, resident.getCivilStatus().name());
            stmt.setString(10, resident.getResidencyStatus().name());
            stmt.setString(11, resident.getEmail());
            stmt.setString(12, resident.getOccupation());
            stmt.setBoolean(13, resident.isRegisteredVoter());
            stmt.setBoolean(14, resident.isHouseholdHead());
            stmt.setString(15, resident.getContactNumber());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Save error: " + e.getMessage());
        }
        return false;
    }

    // Update an existing resident's information
    @Override
    public boolean update(Resident resident) {
        String sql = "UPDATE residents SET resident_code=?, first_name=?, middle_name=?, last_name=?, birth_date=?, sex=?, civil_status=?, residency_status=?, contact_number=? WHERE resident_id=?";
        try (Connection conn = dbFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, resident.getResidentCode());
            stmt.setString(2, resident.getFirstName());
            stmt.setString(3, resident.getMiddleName());
            stmt.setString(4, resident.getLastName());
            stmt.setDate(5, Date.valueOf(resident.getBirthDate()));
            stmt.setString(6, resident.getSex().name());
            stmt.setString(7, resident.getCivilStatus().name());
            stmt.setString(8, resident.getResidencyStatus().name());
            stmt.setString(9, resident.getContactNumber());
            stmt.setInt(10, resident.getResidentId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating resident: " + e.getMessage());
        }
        return false;
    }

    /// Soft delete: set status to INACTIVE instead of removing record
    @Override
    public boolean deactivate(int id) {
        String sql = "UPDATE residents SET residency_status = 'INACTIVE', updated_at = NOW() WHERE resident_id = ?";
        try (Connection conn = dbFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Deactivate error: " + e.getMessage());
        }
        return false;
    }

    // Convert database result row into a Resident object
    private Resident mapToResident(ResultSet rs) throws SQLException {
        Resident r = new Resident();
        r.setResidentId(rs.getInt("resident_id"));
        r.setResidentCode(rs.getString("resident_code"));
        r.setHouseholdId(rs.getObject("household_id", Integer.class));
        r.setSuffix(rs.getString("suffix"));
        r.setFirstName(rs.getString("first_name"));
        r.setMiddleName(rs.getString("middle_name"));
        r.setLastName(rs.getString("last_name"));
        r.setBirthDate(rs.getDate("birth_date").toLocalDate());
        r.setSex(Sex.valueOf(rs.getString("sex")));
        r.setCivilStatus(CivilStatus.valueOf(rs.getString("civil_status")));
        r.setResidencyStatus(ResidencyStatus.valueOf(rs.getString("residency_status")));
        r.setEmail(rs.getString("email"));
        r.setOccupation(rs.getString("occupation"));
        r.setRegisteredVoter(rs.getBoolean("is_registered_voter"));
        r.setHouseholdHead(rs.getBoolean("is_household_head"));
        r.setContactNumber(rs.getString("contact_number"));
        r.setDateRegistered(rs.getDate("date_registered") != null ? rs.getDate("date_registered").toLocalDate() : null);
        r.setUpdatedAt(rs.getTimestamp("updated_at") != null ? LocalDateTime.from(rs.getTimestamp("updated_at").toLocalDateTime().toLocalDate()) : null);
        return r;
    }
}