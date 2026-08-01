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
    public List<ServiceType> getAll() throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM service_types ORDER BY service_name";
        return queryList(sql);
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

    @Override
    public Optional<ServiceType> getByCode(String serviceCode) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM service_types WHERE service_code = ?";
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, serviceCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapServiceType(resultSet)) : Optional.empty();
            }
        }
    }

    @Override
    public boolean save(ServiceType serviceType) throws SQLException {
        String sql = """
                INSERT INTO service_types (
                    service_code, service_name, description, default_fee,
                    expected_processing_days, is_active
                ) VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, serviceType.getServiceCode());
            statement.setString(2, serviceType.getServiceName());
            statement.setString(3, serviceType.getDescription());
            statement.setBigDecimal(4, serviceType.getDefaultFee());
            statement.setInt(5, serviceType.getExpectedProcessingDays());
            statement.setBoolean(6, serviceType.isActive());
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean update(ServiceType serviceType) throws SQLException {
        String sql = """
                UPDATE service_types
                SET service_name = ?, description = ?, default_fee = ?,
                    expected_processing_days = ?, updated_at = NOW()
                WHERE service_type_id = ?
                """;
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, serviceType.getServiceName());
            statement.setString(2, serviceType.getDescription());
            statement.setBigDecimal(3, serviceType.getDefaultFee());
            statement.setInt(4, serviceType.getExpectedProcessingDays());
            statement.setInt(5, serviceType.getServiceTypeId());
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean setActive(int serviceTypeId, boolean active) throws SQLException {
        String sql = """
                UPDATE service_types
                SET is_active = ?, updated_at = NOW()
                WHERE service_type_id = ? AND is_active <> ?
                """;
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBoolean(1, active);
            statement.setInt(2, serviceTypeId);
            statement.setBoolean(3, active);
            return statement.executeUpdate() > 0;
        }
    }

    private List<ServiceType> queryList(String sql) throws SQLException {
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
