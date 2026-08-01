package com.joysistvi.brgyconnectapp.repository;

import com.joysistvi.brgyconnectapp.config.ConnectionFactory;
import com.joysistvi.brgyconnectapp.model.ServiceType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ServiceTypeRepoImpl implements ServiceTypeRepo {
    private static final String COLUMNS = """
            service_type_id, service_code, service_name, description,
            default_fee, expected_processing_days, is_active, created_at, updated_at
            """;

    private final ConnectionFactory connectionFactory;

    public ServiceTypeRepoImpl(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public List<ServiceType> getAllActive() throws SQLException {
        String sql = "SELECT " + COLUMNS + """
                FROM service_types
                WHERE is_active = TRUE
                ORDER BY service_name
                """;
        List<ServiceType> serviceTypes = new ArrayList<>();
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                serviceTypes.add(mapServiceType(resultSet));
            }
        }
        return serviceTypes;
    }

    @Override
    public Optional<ServiceType> getById(int serviceTypeId) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM service_types WHERE service_type_id = ?";
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, serviceTypeId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapServiceType(resultSet)) : Optional.empty();
            }
        }
    }

    private ServiceType mapServiceType(ResultSet resultSet) throws SQLException {
        ServiceType serviceType = new ServiceType();
        serviceType.setServiceTypeId(resultSet.getInt("service_type_id"));
        serviceType.setServiceCode(resultSet.getString("service_code"));
        serviceType.setServiceName(resultSet.getString("service_name"));
        serviceType.setDescription(resultSet.getString("description"));
        serviceType.setDefaultFee(resultSet.getBigDecimal("default_fee"));
        serviceType.setExpectedProcessingDays(resultSet.getInt("expected_processing_days"));
        serviceType.setActive(resultSet.getBoolean("is_active"));
        serviceType.setCreatedAt(toLocalDateTime(resultSet.getTimestamp("created_at")));
        serviceType.setUpdatedAt(toLocalDateTime(resultSet.getTimestamp("updated_at")));
        return serviceType;
    }

    private java.time.LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
