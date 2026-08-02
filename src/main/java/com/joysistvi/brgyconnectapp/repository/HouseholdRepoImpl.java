package com.joysistvi.brgyconnectapp.repository;

import com.joysistvi.brgyconnectapp.config.ConnectionFactory;
import com.joysistvi.brgyconnectapp.model.CivilStatus;
import com.joysistvi.brgyconnectapp.model.Household;
import com.joysistvi.brgyconnectapp.model.HouseholdStatus;
import com.joysistvi.brgyconnectapp.model.Resident;
import com.joysistvi.brgyconnectapp.model.ResidencyStatus;
import com.joysistvi.brgyconnectapp.model.Sex;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HouseholdRepoImpl implements HouseholdRepo {
    private static final String HOUSEHOLD_COLUMNS = """
            household_id, household_code, address_line, purok,
            household_status, created_at, updated_at
            """;

    private final ConnectionFactory connectionFactory;

    public HouseholdRepoImpl(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public List<Household> getAll() throws SQLException {
        String sql = "SELECT " + HOUSEHOLD_COLUMNS + " FROM households ORDER BY household_id";
        List<Household> households = new ArrayList<>();
        try (Connection connection = connectionFactory.openConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                households.add(mapHousehold(resultSet));
            }
        }
        return households;
    }

    @Override
    public Optional<Household> getById(int householdId) throws SQLException {
        String sql = "SELECT " + HOUSEHOLD_COLUMNS + " FROM households WHERE household_id = ?";
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, householdId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapHousehold(resultSet)) : Optional.empty();
            }
        }
    }

    @Override
    public Optional<Household> getByCode(String householdCode) throws SQLException {
        String sql = "SELECT " + HOUSEHOLD_COLUMNS + " FROM households WHERE household_code = ?";
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, householdCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapHousehold(resultSet)) : Optional.empty();
            }
        }
    }

    @Override
    public List<Household> search(String keyword) throws SQLException {
        String sql = "SELECT " + HOUSEHOLD_COLUMNS + """
                FROM households
                WHERE household_code LIKE ? OR address_line LIKE ? OR purok LIKE ?
                ORDER BY household_id
                """;
        List<Household> households = new ArrayList<>();
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            statement.setString(1, pattern);
            statement.setString(2, pattern);
            statement.setString(3, pattern);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    households.add(mapHousehold(resultSet));
                }
            }
        }
        return households;
    }

    @Override
    public boolean save(Household household) throws SQLException {
        String insertSql = """
                INSERT INTO households (
                    household_code, address_line, purok, household_status
                ) VALUES (?, ?, ?, ?)
                """;
        String assignCodeSql = "UPDATE households SET household_code = ? WHERE household_id = ?";

        try (Connection connection = connectionFactory.openConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                int householdId;
                int registrationYear = java.time.LocalDate.now().getYear();
                String temporaryCode = "HH-%04d-%d".formatted(
                        registrationYear,
                        java.util.concurrent.ThreadLocalRandom.current().nextLong(
                                1_000_000_000_000_000_000L,
                                Long.MAX_VALUE));

                try (PreparedStatement statement = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setString(1, temporaryCode);
                    statement.setString(2, household.getAddressLine());
                    statement.setString(3, household.getPurok());
                    statement.setString(4, household.getHouseholdStatus().name());
                    if (statement.executeUpdate() == 0) {
                        connection.rollback();
                        return false;
                    }
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (keys.next()) {
                            householdId = keys.getInt(1);
                        } else {
                            throw new SQLException("Database did not return the generated household ID");
                        }
                    }
                }

                String householdCode = String.format("HH-%04d-%07d", registrationYear, householdId);
                try (PreparedStatement statement = connection.prepareStatement(assignCodeSql)) {
                    statement.setString(1, householdCode);
                    statement.setInt(2, householdId);
                    if (statement.executeUpdate() != 1) {
                        throw new SQLException("Unable to assign the generated household code");
                    }
                }

                connection.commit();
                household.setHouseholdId(householdId);
                household.setHouseholdCode(householdCode);
                return true;
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        }
    }

    @Override
    public boolean update(Household household) throws SQLException {
        String sql = """
                UPDATE households
                SET address_line = ?, purok = ?, household_status = ?, updated_at = NOW()
                WHERE household_id = ?
                """;
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, household.getAddressLine());
            statement.setString(2, household.getPurok());
            statement.setString(3, household.getHouseholdStatus().name());
            statement.setInt(4, household.getHouseholdId());
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deactivate(int householdId) throws SQLException {
        String sql = """
                UPDATE households
                SET household_status = 'INACTIVE', updated_at = NOW()
                WHERE household_id = ? AND household_status <> 'INACTIVE'
                """;
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, householdId);
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public List<Resident> getMembers(int householdId) throws SQLException {
        String sql = """
                SELECT *
                FROM residents
                WHERE household_id = ?
                ORDER BY is_household_head DESC, last_name, first_name
                """;
        List<Resident> members = new ArrayList<>();
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, householdId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    members.add(mapResident(resultSet));
                }
            }
        }
        return members;
    }

    @Override
    public boolean addMember(int householdId, int residentId) throws SQLException {
        String sql = """
                UPDATE residents
                SET household_id = ?, is_household_head = FALSE, updated_at = NOW()
                WHERE resident_id = ? AND residency_status = 'ACTIVE'
                """;
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, householdId);
            statement.setInt(2, residentId);
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean removeMember(int householdId, int residentId) throws SQLException {
        String sql = """
                UPDATE residents
                SET household_id = NULL, is_household_head = FALSE, updated_at = NOW()
                WHERE resident_id = ? AND household_id = ?
                """;
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, residentId);
            statement.setInt(2, householdId);
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean assignHouseholdHead(int householdId, int residentId) throws SQLException {
        String clearHeadSql = """
                UPDATE residents
                SET is_household_head = FALSE, updated_at = NOW()
                WHERE household_id = ? AND is_household_head = TRUE
                """;
        String assignHeadSql = """
                UPDATE residents
                SET is_household_head = TRUE, updated_at = NOW()
                WHERE resident_id = ? AND household_id = ? AND residency_status = 'ACTIVE'
                """;

        try (Connection connection = connectionFactory.openConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement clearStatement = connection.prepareStatement(clearHeadSql);
                 PreparedStatement assignStatement = connection.prepareStatement(assignHeadSql)) {
                clearStatement.setInt(1, householdId);
                clearStatement.executeUpdate();

                assignStatement.setInt(1, residentId);
                assignStatement.setInt(2, householdId);
                boolean assigned = assignStatement.executeUpdate() > 0;
                if (!assigned) {
                    connection.rollback();
                    return false;
                }

                connection.commit();
                return true;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private Household mapHousehold(ResultSet resultSet) throws SQLException {
        Household household = new Household();
        household.setHouseholdId(resultSet.getInt("household_id"));
        household.setHouseholdCode(resultSet.getString("household_code"));
        household.setAddressLine(resultSet.getString("address_line"));
        household.setPurok(resultSet.getString("purok"));
        household.setHouseholdStatus(HouseholdStatus.valueOf(resultSet.getString("household_status")));
        household.setCreatedAt(toLocalDateTime(resultSet.getTimestamp("created_at")));
        household.setUpdatedAt(toLocalDateTime(resultSet.getTimestamp("updated_at")));
        return household;
    }

    private Resident mapResident(ResultSet resultSet) throws SQLException {
        Resident resident = new Resident();
        resident.setResidentId(resultSet.getInt("resident_id"));
        resident.setResidentCode(resultSet.getString("resident_code"));
        resident.setHouseholdId(resultSet.getObject("household_id", Integer.class));
        resident.setFirstName(resultSet.getString("first_name"));
        resident.setMiddleName(resultSet.getString("middle_name"));
        resident.setLastName(resultSet.getString("last_name"));
        resident.setSuffix(resultSet.getString("suffix"));
        Date birthDate = resultSet.getDate("birth_date");
        resident.setBirthDate(birthDate == null ? null : birthDate.toLocalDate());
        resident.setSex(Sex.valueOf(resultSet.getString("sex")));
        resident.setCivilStatus(CivilStatus.valueOf(resultSet.getString("civil_status")));
        resident.setContactNumber(resultSet.getString("contact_number"));
        resident.setEmail(resultSet.getString("email"));
        resident.setOccupation(resultSet.getString("occupation"));
        resident.setRegisteredVoter(resultSet.getBoolean("is_registered_voter"));
        resident.setHouseholdHead(resultSet.getBoolean("is_household_head"));
        resident.setResidencyStatus(ResidencyStatus.valueOf(resultSet.getString("residency_status")));
        Date registered = resultSet.getDate("date_registered");
        resident.setDateRegistered(registered == null ? null : registered.toLocalDate());
        resident.setCreatedAt(toLocalDateTime(resultSet.getTimestamp("created_at")));
        resident.setUpdatedAt(toLocalDateTime(resultSet.getTimestamp("updated_at")));
        return resident;
    }

    private java.time.LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
