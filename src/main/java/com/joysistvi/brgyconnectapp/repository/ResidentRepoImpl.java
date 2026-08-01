package com.joysistvi.brgyconnectapp.repository;

import com.joysistvi.brgyconnectapp.config.ConnectionFactory;
import com.joysistvi.brgyconnectapp.model.CivilStatus;
import com.joysistvi.brgyconnectapp.model.Resident;
import com.joysistvi.brgyconnectapp.model.ResidencyStatus;
import com.joysistvi.brgyconnectapp.model.Sex;
import com.joysistvi.brgyconnectapp.validation.ResidentFieldValidator;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class ResidentRepoImpl implements ResidentRepo {
    private final ConnectionFactory dbFactory;

    public ResidentRepoImpl(ConnectionFactory dbFactory) {
        this.dbFactory = dbFactory;
    }

    // Get all residents with proper error logging
    @Override
    public List<Resident> getAll() throws SQLException {
        List<Resident> list = new ArrayList<>();
        String sql = "SELECT * FROM residents ORDER BY last_name, first_name";
        try (Connection conn = dbFactory.openConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapToResident(rs));
        }
        return list;
    }

    @Override
    public List<Resident> getAllActive() throws SQLException {
        List<Resident> residents = new ArrayList<>();
        String sql = """
        SELECT *
        FROM residents
        WHERE residency_status = 'ACTIVE'
        ORDER BY last_name, first_name
        """;

        try (Connection conn = dbFactory.openConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                residents.add(mapToResident(rs));
            }
        }

        return residents;
    }

    // Get one resident using their primary key ID
    @Override
    public Optional<Resident> getById(int id) throws SQLException {
        try (Connection conn = dbFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM residents WHERE resident_id = ?")) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapToResident(rs)) : Optional.empty();
            }
        }
    }

    // Get one resident using their unique resident code
    @Override
    public Optional<Resident> getByCode(String code) throws SQLException {
        try (Connection conn = dbFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM residents WHERE resident_code = ?")) {
            stmt.setString(1, code);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapToResident(rs)) : Optional.empty();
            }
        }
    }

    // Search for residents — matches first name, last name, or resident code
    @Override
    public List<Resident> searchByNameOrCode(String keyword) throws SQLException {
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
        }
        return list;
    }

    // Save a resident and derive its immutable code from the database-generated ID.
    @Override
    public boolean save(Resident resident) throws SQLException {
        String insertSql = """
            INSERT INTO residents (
                resident_code, household_id, suffix, first_name, middle_name, last_name,
                birth_date, sex, civil_status, residency_status, email, occupation,
                is_registered_voter, is_household_head, contact_number, date_registered
            ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?, NOW())
            """;
        String assignCodeSql = "UPDATE residents SET resident_code = ? WHERE resident_id = ?";

        try (Connection conn = dbFactory.openConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                int residentId;
                int registrationYear = ResidentFieldValidator.currentResidentCodeYear();
                String temporaryCode = "RES-%04d-%d".formatted(
                        registrationYear,
                        ThreadLocalRandom.current().nextLong(
                                1_000_000_000_000_000_000L,
                                Long.MAX_VALUE));

                try (PreparedStatement stmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, temporaryCode);
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
                    if (stmt.executeUpdate() == 0) {
                        conn.rollback();
                        return false;
                    }

                    try (ResultSet keys = stmt.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Database did not return the generated resident ID");
                        }
                        residentId = keys.getInt(1);
                    }
                }

                String residentCode = ResidentFieldValidator.generateResidentCode(residentId, registrationYear);
                try (PreparedStatement stmt = conn.prepareStatement(assignCodeSql)) {
                    stmt.setString(1, residentCode);
                    stmt.setInt(2, residentId);
                    if (stmt.executeUpdate() != 1) {
                        throw new SQLException("Unable to assign the generated resident code");
                    }
                }

                conn.commit();
                resident.setResidentId(residentId);
                resident.setResidentCode(residentCode);
                return true;
            } catch (SQLException | RuntimeException exception) {
                conn.rollback();
                throw exception;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        }
    }

    // Update an existing resident's information
    @Override
    public boolean update(Resident resident) throws SQLException {
        String sql = """
        UPDATE residents SET
            first_name = ?,
            middle_name = ?,
            last_name = ?,
            suffix = ?,
            birth_date = ?,
            sex = ?,
            civil_status = ?,
            residency_status = ?,
            contact_number = ?,
            email = ?,
            household_id = ?,
            occupation = ?,
            is_registered_voter = ?,
            is_household_head = ?,
            updated_at = NOW()
        WHERE resident_id = ?
        """;
        try (Connection conn = dbFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set values in EXACT order matching the SQL above
            stmt.setString(1, resident.getFirstName());
            stmt.setString(2, resident.getMiddleName());
            stmt.setString(3, resident.getLastName());
            stmt.setString(4, resident.getSuffix());
            stmt.setDate(5, Date.valueOf(resident.getBirthDate()));
            stmt.setString(6, resident.getSex().name());
            stmt.setString(7, resident.getCivilStatus().name());
            stmt.setString(8, resident.getResidencyStatus().name());
            stmt.setString(9, resident.getContactNumber());
            stmt.setString(10, resident.getEmail());
            stmt.setObject(11, resident.getHouseholdId());
            stmt.setString(12, resident.getOccupation());
            stmt.setBoolean(13, resident.isRegisteredVoter());
            stmt.setBoolean(14, resident.isHouseholdHead());
            stmt.setInt(15, resident.getResidentId());

            return stmt.executeUpdate() > 0;
        }
    }

    /// Soft delete: set status to INACTIVE instead of removing record
    @Override
    public boolean deactivate(int id) throws SQLException {
        String sql = "UPDATE residents SET residency_status = 'INACTIVE', updated_at = NOW() WHERE resident_id = ?";
        try (Connection conn = dbFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    // Convert database result row into a Resident object
    private Resident mapToResident(ResultSet rs) throws SQLException {
        Resident r = new Resident();
        r.setResidentId(rs.getObject("resident_id", Integer.class));
        r.setResidentCode(rs.getString("resident_code"));
        r.setHouseholdId(rs.getObject("household_id", Integer.class));
        r.setFirstName(rs.getString("first_name"));
        r.setMiddleName(rs.getString("middle_name"));
        r.setLastName(rs.getString("last_name"));
        r.setSuffix(rs.getString("suffix"));
        r.setBirthDate(rs.getDate("birth_date").toLocalDate());
        r.setSex(Sex.valueOf(rs.getString("sex")));
        r.setCivilStatus(CivilStatus.valueOf(rs.getString("civil_status")));
        r.setContactNumber(rs.getString("contact_number"));
        r.setEmail(rs.getString("email"));
        r.setOccupation(rs.getString("occupation"));
        r.setRegisteredVoter(rs.getBoolean("is_registered_voter"));
        r.setHouseholdHead(rs.getBoolean("is_household_head"));
        r.setResidencyStatus(ResidencyStatus.valueOf(rs.getString("residency_status")));
        r.setDateRegistered(rs.getDate("date_registered").toLocalDate());

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) r.setCreatedAt(created.toLocalDateTime());

        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) r.setUpdatedAt(updated.toLocalDateTime());

        return r;
    }
}
