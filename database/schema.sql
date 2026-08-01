Exit code: 0
Wall time: 0.4 seconds
Output:
-- BarangayConnect development database schema
-- Compatible with MariaDB 10.4+ and MySQL 8.0+.
--
-- WARNING: This script drops and recreates all BarangayConnect tables.
-- Use it only for local development or a database that may be reset.

CREATE DATABASE IF NOT EXISTS barangayconnect_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE barangayconnect_db;

SET NAMES utf8mb4;
SET time_zone = '+08:00';
SET FOREIGN_KEY_CHECKS = 0;

DROP VIEW IF EXISTS vw_service_request_summary;
DROP TABLE IF EXISTS request_status_history;
DROP TABLE IF EXISTS activity_logs;
DROP TABLE IF EXISTS service_requests;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS residents;
DROP TABLE IF EXISTS service_types;
DROP TABLE IF EXISTS households;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE households (
    household_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    household_code VARCHAR(30) NOT NULL,
    address_line VARCHAR(255) NOT NULL,
    purok VARCHAR(100) NULL,
    household_status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_households PRIMARY KEY (household_id),
    CONSTRAINT uq_households_code UNIQUE (household_code),
    INDEX idx_households_purok (purok),
    INDEX idx_households_status (household_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE residents (
    resident_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    resident_code VARCHAR(30) NOT NULL,
    household_id INT UNSIGNED NULL,
    first_name VARCHAR(80) NOT NULL,
    middle_name VARCHAR(80) NULL,
    last_name VARCHAR(80) NOT NULL,
    suffix VARCHAR(20) NULL,
    birth_date DATE NOT NULL,
    sex ENUM('MALE', 'FEMALE', 'PREFER_NOT_TO_SAY') NOT NULL,
    civil_status ENUM('SINGLE', 'MARRIED', 'WIDOWED', 'SEPARATED', 'OTHER') NOT NULL,
    contact_number VARCHAR(30) NULL,
    email VARCHAR(120) NULL,
    occupation VARCHAR(120) NULL,
    is_registered_voter BOOLEAN NOT NULL DEFAULT FALSE,
    is_household_head BOOLEAN NOT NULL DEFAULT FALSE,
    residency_status ENUM('ACTIVE', 'TRANSFERRED', 'DECEASED', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    date_registered DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_residents PRIMARY KEY (resident_id),
    CONSTRAINT uq_residents_code UNIQUE (resident_code),
    CONSTRAINT chk_residents_code_format
        CHECK (resident_code REGEXP '^RES-[0-9]{4}-[0-9]{6,}$'),
    CONSTRAINT fk_residents_household
        FOREIGN KEY (household_id) REFERENCES households (household_id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX idx_residents_name (last_name, first_name),
    INDEX idx_residents_household (household_id),
    INDEX idx_residents_status (residency_status),
    INDEX idx_residents_birth_date (birth_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE users (
    user_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    resident_id INT UNSIGNED NULL,
    username VARCHAR(50) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(150) NOT NULL,
    role ENUM('RESIDENT', 'STAFF', 'ADMIN') NOT NULL,
    account_status ENUM('PENDING_ACTIVATION', 'ACTIVE', 'INACTIVE', 'LOCKED')
        NOT NULL DEFAULT 'PENDING_ACTIVATION',
    failed_login_attempts SMALLINT UNSIGNED NOT NULL DEFAULT 0,
    last_login_at DATETIME NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_users PRIMARY KEY (user_id),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_resident UNIQUE (resident_id),
    CONSTRAINT fk_users_resident
        FOREIGN KEY (resident_id) REFERENCES residents (resident_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_users_role_status (role, account_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE service_types (
    service_type_id SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT,
    service_code VARCHAR(30) NOT NULL,
    service_name VARCHAR(120) NOT NULL,
    description VARCHAR(500) NULL,
    default_fee DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    expected_processing_days SMALLINT UNSIGNED NOT NULL DEFAULT 1,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_service_types PRIMARY KEY (service_type_id),
    CONSTRAINT uq_service_types_code UNIQUE (service_code),
    CONSTRAINT uq_service_types_name UNIQUE (service_name),
    CONSTRAINT chk_service_types_fee CHECK (default_fee >= 0),
    CONSTRAINT chk_service_types_processing_days CHECK (expected_processing_days >= 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE service_requests (
    request_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    request_number VARCHAR(40) NOT NULL,
    resident_id INT UNSIGNED NOT NULL,
    service_type_id SMALLINT UNSIGNED NOT NULL,
    purpose VARCHAR(500) NOT NULL,
    request_date DATE NOT NULL,
    service_fee_snapshot DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    status ENUM('PENDING', 'UNDER_REVIEW', 'APPROVED', 'RELEASED', 'REJECTED', 'CANCELLED')
        NOT NULL DEFAULT 'PENDING',
    remarks VARCHAR(1000) NULL,
    created_by_user_id INT UNSIGNED NOT NULL,
    processed_by_user_id INT UNSIGNED NULL,
    processed_at DATETIME NULL,
    released_at DATETIME NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_service_requests PRIMARY KEY (request_id),
    CONSTRAINT uq_service_requests_number UNIQUE (request_number),
    CONSTRAINT chk_service_requests_fee CHECK (service_fee_snapshot >= 0),
    CONSTRAINT fk_requests_resident
        FOREIGN KEY (resident_id) REFERENCES residents (resident_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_requests_service_type
        FOREIGN KEY (service_type_id) REFERENCES service_types (service_type_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_requests_created_by
        FOREIGN KEY (created_by_user_id) REFERENCES users (user_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_requests_processed_by
        FOREIGN KEY (processed_by_user_id) REFERENCES users (user_id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX idx_requests_resident (resident_id),
    INDEX idx_requests_service_type (service_type_id),
    INDEX idx_requests_status_date (status, request_date),
    INDEX idx_requests_created_by (created_by_user_id),
    INDEX idx_requests_processed_by (processed_by_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE request_status_history (
    history_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    request_id BIGINT UNSIGNED NOT NULL,
    old_status ENUM('PENDING', 'UNDER_REVIEW', 'APPROVED', 'RELEASED', 'REJECTED', 'CANCELLED') NULL,
    new_status ENUM('PENDING', 'UNDER_REVIEW', 'APPROVED', 'RELEASED', 'REJECTED', 'CANCELLED') NOT NULL,
    remarks VARCHAR(1000) NULL,
    changed_by_user_id INT UNSIGNED NOT NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_request_status_history PRIMARY KEY (history_id),
    CONSTRAINT fk_history_request
        FOREIGN KEY (request_id) REFERENCES service_requests (request_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_history_changed_by
        FOREIGN KEY (changed_by_user_id) REFERENCES users (user_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_history_request_date (request_id, changed_at),
    INDEX idx_history_changed_by (changed_by_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE activity_logs (
    activity_log_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id INT UNSIGNED NULL,
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT UNSIGNED NULL,
    description VARCHAR(1000) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_activity_logs PRIMARY KEY (activity_log_id),
    CONSTRAINT fk_activity_logs_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX idx_activity_user_date (user_id, created_at),
    INDEX idx_activity_entity (entity_type, entity_id),
    INDEX idx_activity_action_date (action, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- The view uses the importing account instead of a machine-specific DEFINER.
CREATE SQL SECURITY INVOKER VIEW vw_service_request_summary AS
SELECT
    sr.request_id,
    sr.request_number,
    r.resident_code,
    CONCAT_WS(' ', r.first_name, NULLIF(r.middle_name, ''), r.last_name, NULLIF(r.suffix, '')) AS resident_name,
    st.service_code,
    st.service_name,
    sr.request_date,
    sr.status,
    sr.service_fee_snapshot,
    creator.display_name AS created_by,
    processor.display_name AS processed_by,
    sr.processed_at,
    sr.released_at
FROM service_requests sr
JOIN residents r ON r.resident_id = sr.resident_id
JOIN service_types st ON st.service_type_id = sr.service_type_id
JOIN users creator ON creator.user_id = sr.created_by_user_id
LEFT JOIN users processor ON processor.user_id = sr.processed_by_user_id;
