package com.joysistvi.brgyconnectapp.repository;

import com.joysistvi.brgyconnectapp.config.ConnectionFactory;
import com.joysistvi.brgyconnectapp.config.DbConnection;
import com.joysistvi.brgyconnectapp.model.ServiceType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ServiceTypeRepoImpl implements ServiceTypeRepo {
    private final ConnectionFactory dbFactory = new DbConnection();

    @Override
    public List<ServiceType> getAllActive() {
        List<ServiceType> list = new ArrayList<>();
        String sql = "SELECT * FROM service_types WHERE active = true ORDER BY service_name";
        try (Connection conn = dbFactory.openConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapToType(rs));
        } catch (SQLException e) {
            System.err.println("Error fetching active services: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<ServiceType> getAll() {
        List<ServiceType> list = new ArrayList<>();
        String sql = "SELECT * FROM service_types ORDER BY service_name";
        try (Connection conn = dbFactory.openConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapToType(rs));
        } catch (SQLException e) {
            System.err.println("Error fetching all services: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Optional<ServiceType> getById(Integer id) {
        try (Connection conn = dbFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM service_types WHERE service_type_id = ?")) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapToType(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            System.err.println("Error fetching service by ID: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<ServiceType> getByCode(String code) {
        try (Connection conn = dbFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM service_types WHERE service_code = ?")) {
            stmt.setString(1, code);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapToType(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            System.err.println("Error fetching service by code: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<ServiceType> getByName(String name) {
        try (Connection conn = dbFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM service_types WHERE service_name = ?")) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapToType(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            System.err.println("Error fetching service by name: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public boolean save(ServiceType type) {
        // Prevent duplicate code or name
        if (getByCode(type.getServiceCode()).isPresent() || getByName(type.getServiceName()).isPresent())
            return false;

        String sql = """
            INSERT INTO service_types (
                service_code, service_name, description, default_fee,
                expected_processing_days, is_active, created_at, updated_at
            ) VALUES (?,?,?,?,?,?, NOW(), NOW())
            """;
        try (Connection conn = dbFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, type.getServiceCode());
            stmt.setString(2, type.getServiceName());
            stmt.setString(3, type.getDescription());
            stmt.setBigDecimal(4, type.getDefaultFee());
            stmt.setInt(5, type.getExpectedProcessingDays());
            stmt.setBoolean(6, type.isActive());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error saving service type: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean update(ServiceType type) {
        String sql = """
            UPDATE service_types SET
                service_code=?, service_name=?, description=?, default_fee=?,
                expected_processing_days=?, is_active=?, updated_at=NOW()
            WHERE service_type_id=?
            """;
        try (Connection conn = dbFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, type.getServiceCode());
            stmt.setString(2, type.getServiceName());
            stmt.setString(3, type.getDescription());
            stmt.setBigDecimal(4, type.getDefaultFee());
            stmt.setInt(5, type.getExpectedProcessingDays());
            stmt.setBoolean(6, type.isActive());
            stmt.setInt(7, type.getServiceTypeId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating service type: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean deactivate(Integer id) {
        // Soft delete: keep record but mark inactive
        String sql = "UPDATE service_types SET is_active = false, updated_at = NOW() WHERE service_type_id = ?";
        try (Connection conn = dbFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deactivating service type: " + e.getMessage());
        }
        return false;
    }

    // Convert database row to ServiceType object
    private ServiceType mapToType(ResultSet rs) throws SQLException {
        ServiceType t = new ServiceType();
        t.setServiceTypeId(rs.getObject("service_type_id", Integer.class));
        t.setServiceCode(rs.getString("service_code"));
        t.setServiceName(rs.getString("service_name"));
        t.setDescription(rs.getString("description"));
        t.setDefaultFee(rs.getBigDecimal("default_fee"));
        t.setExpectedProcessingDays(rs.getInt("expected_processing_days"));
        t.setActive(rs.getBoolean("is_active"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) t.setCreatedAt(createdAt.toLocalDateTime());

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) t.setUpdatedAt(updatedAt.toLocalDateTime());

        return t;
    }
}