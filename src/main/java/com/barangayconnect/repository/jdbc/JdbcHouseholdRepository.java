package com.barangayconnect.repository.jdbc;

import com.barangayconnect.config.ConnectionFactory;
import com.barangayconnect.model.enums.HouseholdStatus;
import com.barangayconnect.model.records.Household;
import com.barangayconnect.repository.contracts.HouseholdRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class JdbcHouseholdRepository implements HouseholdRepository {
    private static final String SELECT_COLUMNS = """
            household_id, household_code, address_line, purok,
            household_status, created_at, updated_at
            """;

    private final ConnectionFactory connectionFactory;

    public JdbcHouseholdRepository(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Household save(Household household) throws SQLException {
        return household.id() == null ? insert(household) : update(household);
    }

    @Override
    public Optional<Household> findById(int id) throws SQLException {
        return findOne("SELECT " + SELECT_COLUMNS + " FROM households WHERE household_id = ?", id);
    }

    @Override
    public Optional<Household> findByCode(String code) throws SQLException {
        return findOne("SELECT " + SELECT_COLUMNS + " FROM households WHERE household_code = ?", code);
    }

    @Override
    public List<Household> findAll() throws SQLException {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM households ORDER BY household_code";
        try (Connection connection = connectionFactory.openConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            List<Household> households = new ArrayList<>();
            while (resultSet.next()) {
                households.add(map(resultSet));
            }
            return households;
        }
    }

    private Household insert(Household household) throws SQLException {
        String sql = """
                INSERT INTO households
                    (household_code, address_line, purok, household_status)
                VALUES (?, ?, ?, ?)
                """;
        try (Connection connection = connectionFactory.openConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindMutableFields(statement, household);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Creating household did not return an identifier.");
                }
                return findById(keys.getInt(1))
                        .orElseThrow(() -> new SQLException("Created household could not be loaded."));
            }
        }
    }

    private Household update(Household household) throws SQLException {
        String sql = """
                UPDATE households
                SET household_code = ?, address_line = ?, purok = ?, household_status = ?
                WHERE household_id = ?
                """;
        try (Connection connection = connectionFactory.openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            bindMutableFields(statement, household);
            statement.setInt(5, household.id());
            if (statement.executeUpdate() == 0) {
                throw new SQLException("Household was not found.");
            }
        }
        return findById(household.id())
                .orElseThrow(() -> new SQLException("Updated household could not be loaded."));
    }

    private Optional<Household> findOne(String sql, Object value) throws SQLException {
        try (Connection connection = connectionFactory.openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, value);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(map(resultSet)) : Optional.empty();
            }
        }
    }

    private static void bindMutableFields(PreparedStatement statement, Household household)
            throws SQLException {
        statement.setString(1, household.code());
        statement.setString(2, household.addressLine());
        statement.setString(3, household.purok());
        statement.setString(4, household.status().name());
    }

    private static Household map(ResultSet row) throws SQLException {
        return new Household(
                row.getInt("household_id"),
                row.getString("household_code"),
                row.getString("address_line"),
                row.getString("purok"),
                HouseholdStatus.valueOf(row.getString("household_status")),
                row.getTimestamp("created_at").toLocalDateTime(),
                row.getTimestamp("updated_at").toLocalDateTime());
    }
}
