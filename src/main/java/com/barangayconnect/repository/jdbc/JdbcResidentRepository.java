package com.barangayconnect.repository.jdbc;

import com.barangayconnect.config.ConnectionFactory;
import com.barangayconnect.model.enums.CivilStatus;
import com.barangayconnect.model.enums.ResidencyStatus;
import com.barangayconnect.model.enums.Sex;
import com.barangayconnect.model.records.Resident;
import com.barangayconnect.repository.contracts.ResidentRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class JdbcResidentRepository implements ResidentRepository {
    private static final String SELECT_COLUMNS = """
            resident_id, resident_code, household_id, first_name, middle_name,
            last_name, suffix, birth_date, sex, civil_status, contact_number,
            email, occupation, is_registered_voter, is_household_head,
            residency_status, date_registered, created_at, updated_at
            """;

    private final ConnectionFactory connectionFactory;

    public JdbcResidentRepository(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Resident save(Resident resident) throws SQLException {
        return resident.id() == null ? insert(resident) : update(resident);
    }

    @Override
    public Optional<Resident> findById(int id) throws SQLException {
        return findOne("SELECT " + SELECT_COLUMNS + " FROM residents WHERE resident_id = ?", id);
    }

    @Override
    public Optional<Resident> findByCode(String code) throws SQLException {
        return findOne("SELECT " + SELECT_COLUMNS + " FROM residents WHERE resident_code = ?", code);
    }

    @Override
    public List<Resident> findByHouseholdId(int householdId) throws SQLException {
        String sql = "SELECT " + SELECT_COLUMNS
                + " FROM residents WHERE household_id = ? ORDER BY last_name, first_name";
        try (Connection connection = connectionFactory.openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, householdId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Resident> residents = new ArrayList<>();
                while (resultSet.next()) {
                    residents.add(map(resultSet));
                }
                return residents;
            }
        }
    }

    private Resident insert(Resident resident) throws SQLException {
        String sql = """
                INSERT INTO residents (
                    resident_code, household_id, first_name, middle_name,
                    last_name, suffix, birth_date, sex, civil_status,
                    contact_number, email, occupation, is_registered_voter,
                    is_household_head, residency_status, date_registered
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = connectionFactory.openConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindMutableFields(statement, resident);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Creating resident did not return an identifier.");
                }
                return findById(keys.getInt(1))
                        .orElseThrow(() -> new SQLException("Created resident could not be loaded."));
            }
        }
    }

    private Resident update(Resident resident) throws SQLException {
        String sql = """
                UPDATE residents SET
                    resident_code = ?, household_id = ?, first_name = ?,
                    middle_name = ?, last_name = ?, suffix = ?, birth_date = ?,
                    sex = ?, civil_status = ?, contact_number = ?, email = ?,
                    occupation = ?, is_registered_voter = ?,
                    is_household_head = ?, residency_status = ?,
                    date_registered = ?
                WHERE resident_id = ?
                """;
        try (Connection connection = connectionFactory.openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            bindMutableFields(statement, resident);
            statement.setInt(17, resident.id());
            if (statement.executeUpdate() == 0) {
                throw new SQLException("Resident was not found.");
            }
        }
        return findById(resident.id())
                .orElseThrow(() -> new SQLException("Updated resident could not be loaded."));
    }

    private Optional<Resident> findOne(String sql, Object value) throws SQLException {
        try (Connection connection = connectionFactory.openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, value);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(map(resultSet)) : Optional.empty();
            }
        }
    }

    private static void bindMutableFields(PreparedStatement statement, Resident resident)
            throws SQLException {
        statement.setString(1, resident.code());
        if (resident.householdId() == null) {
            statement.setNull(2, Types.INTEGER);
        } else {
            statement.setInt(2, resident.householdId());
        }
        statement.setString(3, resident.firstName());
        statement.setString(4, resident.middleName());
        statement.setString(5, resident.lastName());
        statement.setString(6, resident.suffix());
        statement.setObject(7, resident.birthDate());
        statement.setString(8, resident.sex().name());
        statement.setString(9, resident.civilStatus().name());
        statement.setString(10, resident.contactNumber());
        statement.setString(11, resident.email());
        statement.setString(12, resident.occupation());
        statement.setBoolean(13, resident.registeredVoter());
        statement.setBoolean(14, resident.householdHead());
        statement.setString(15, resident.status().name());
        statement.setObject(16, resident.dateRegistered());
    }

    private static Resident map(ResultSet row) throws SQLException {
        int householdId = row.getInt("household_id");
        return new Resident(
                row.getInt("resident_id"),
                row.getString("resident_code"),
                row.wasNull() ? null : householdId,
                row.getString("first_name"),
                row.getString("middle_name"),
                row.getString("last_name"),
                row.getString("suffix"),
                row.getObject("birth_date", java.time.LocalDate.class),
                Sex.valueOf(row.getString("sex")),
                CivilStatus.valueOf(row.getString("civil_status")),
                row.getString("contact_number"),
                row.getString("email"),
                row.getString("occupation"),
                row.getBoolean("is_registered_voter"),
                row.getBoolean("is_household_head"),
                ResidencyStatus.valueOf(row.getString("residency_status")),
                row.getObject("date_registered", java.time.LocalDate.class),
                row.getTimestamp("created_at").toLocalDateTime(),
                row.getTimestamp("updated_at").toLocalDateTime());
    }
}
