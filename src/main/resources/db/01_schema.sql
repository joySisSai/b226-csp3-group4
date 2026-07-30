-- ============================================================================
-- BarangayConnect schema
-- Target: MySQL 8.0+
-- Safe behavior: creates the database and objects without dropping data.
-- ============================================================================

CREATE DATABASE IF NOT EXISTS barangayconnect_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE barangayconnect_db;

CREATE TABLE IF NOT EXISTS households (
    household_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    household_code VARCHAR(30) NOT NULL,
    address_line VARCHAR(255) NOT NULL,
    purok VARCHAR(100) NULL,
    household_status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_households_code UNIQUE (household_code),
    INDEX idx_households_purok (purok),
    INDEX idx_households_status (household_status)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS residents (
    resident_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    resident_code VARCHAR(30) NOT NULL,
    household_id INT UNSIGNED NULL,
    first_name VARCHAR(80) NOT NULL,
    middle_name VARCHAR(80) NULL,
    last_name VARCHAR(80) NOT NULL,
    suffix VARCHAR(20) NULL,
    birth_date DATE NOT NULL,
    sex ENUM('MALE', 'FEMALE', 'PREFER_NOT_TO_SAY') NOT NULL,
    civil_status ENUM(
        'SINGLE',
        'MARRIED',
        'WIDOWED',
        'SEPARATED',
        'OTHER'
    ) NOT NULL,
    contact_number VARCHAR(30) NULL,
    email VARCHAR(120) NULL,
    occupation VARCHAR(120) NULL,
    is_registered_voter BOOLEAN NOT NULL DEFAULT FALSE,
    is_household_head BOOLEAN NOT NULL DEFAULT FALSE,
    residency_status ENUM(
        'ACTIVE',
        'TRANSFERRED',
        'DECEASED',
        'INACTIVE'
    ) NOT NULL DEFAULT 'ACTIVE',
    date_registered DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_residents_code UNIQUE (resident_code),
    CONSTRAINT fk_residents_household
        FOREIGN KEY (household_id)
        REFERENCES households(household_id)
        ON UPDATE CASCADE
        ON DELETE SET NULL,
    INDEX idx_residents_name (last_name, first_name),
    INDEX idx_residents_household (household_id),
    INDEX idx_residents_status (residency_status),
    INDEX idx_residents_birth_date (birth_date)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS users (
    user_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    resident_id INT UNSIGNED NULL,
    username VARCHAR(50) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(150) NOT NULL,
    role ENUM('RESIDENT', 'STAFF', 'ADMIN') NOT NULL,
    account_status ENUM(
        'PENDING_ACTIVATION',
        'ACTIVE',
        'INACTIVE',
        'LOCKED'
    ) NOT NULL DEFAULT 'PENDING_ACTIVATION',
    failed_login_attempts SMALLINT UNSIGNED NOT NULL DEFAULT 0,
    last_login_at DATETIME NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_resident UNIQUE (resident_id),
    CONSTRAINT fk_users_resident
        FOREIGN KEY (resident_id)
        REFERENCES residents(resident_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT chk_users_role_resident_link CHECK (
        (role = 'RESIDENT' AND resident_id IS NOT NULL)
        OR
        (role IN ('STAFF', 'ADMIN') AND resident_id IS NULL)
    ),
    INDEX idx_users_role_status (role, account_status)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS service_types (
    service_type_id SMALLINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    service_code VARCHAR(30) NOT NULL,
    service_name VARCHAR(120) NOT NULL,
    description VARCHAR(500) NULL,
    default_fee DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    expected_processing_days SMALLINT UNSIGNED NOT NULL DEFAULT 1,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_service_types_code UNIQUE (service_code),
    CONSTRAINT uq_service_types_name UNIQUE (service_name),
    CONSTRAINT chk_service_types_fee CHECK (default_fee >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS service_requests (
    request_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    request_number VARCHAR(40) NOT NULL,
    resident_id INT UNSIGNED NOT NULL,
    service_type_id SMALLINT UNSIGNED NOT NULL,
    purpose VARCHAR(500) NOT NULL,
    request_date DATE NOT NULL,
    service_fee_snapshot DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    status ENUM(
        'PENDING',
        'UNDER_REVIEW',
        'APPROVED',
        'RELEASED',
        'REJECTED',
        'CANCELLED'
    ) NOT NULL DEFAULT 'PENDING',
    remarks VARCHAR(1000) NULL,
    created_by_user_id INT UNSIGNED NOT NULL,
    processed_by_user_id INT UNSIGNED NULL,
    processed_at DATETIME NULL,
    released_at DATETIME NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_service_requests_number UNIQUE (request_number),
    CONSTRAINT chk_service_requests_fee CHECK (service_fee_snapshot >= 0),
    CONSTRAINT fk_requests_resident
        FOREIGN KEY (resident_id)
        REFERENCES residents(resident_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_requests_service_type
        FOREIGN KEY (service_type_id)
        REFERENCES service_types(service_type_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_requests_created_by
        FOREIGN KEY (created_by_user_id)
        REFERENCES users(user_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_requests_processed_by
        FOREIGN KEY (processed_by_user_id)
        REFERENCES users(user_id)
        ON UPDATE CASCADE
        ON DELETE SET NULL,
    INDEX idx_requests_resident (resident_id),
    INDEX idx_requests_service_type (service_type_id),
    INDEX idx_requests_status_date (status, request_date),
    INDEX idx_requests_created_by (created_by_user_id),
    INDEX idx_requests_processed_by (processed_by_user_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS request_status_history (
    history_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    request_id BIGINT UNSIGNED NOT NULL,
    old_status ENUM(
        'PENDING',
        'UNDER_REVIEW',
        'APPROVED',
        'RELEASED',
        'REJECTED',
        'CANCELLED'
    ) NULL,
    new_status ENUM(
        'PENDING',
        'UNDER_REVIEW',
        'APPROVED',
        'RELEASED',
        'REJECTED',
        'CANCELLED'
    ) NOT NULL,
    remarks VARCHAR(1000) NULL,
    changed_by_user_id INT UNSIGNED NOT NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_history_request
        FOREIGN KEY (request_id)
        REFERENCES service_requests(request_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_history_changed_by
        FOREIGN KEY (changed_by_user_id)
        REFERENCES users(user_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    INDEX idx_history_request_date (request_id, changed_at),
    INDEX idx_history_changed_by (changed_by_user_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS activity_logs (
    activity_log_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNSIGNED NULL,
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT UNSIGNED NULL,
    description VARCHAR(1000) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_activity_logs_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON UPDATE CASCADE
        ON DELETE SET NULL,
    INDEX idx_activity_user_date (user_id, created_at),
    INDEX idx_activity_entity (entity_type, entity_id),
    INDEX idx_activity_action_date (action, created_at)
) ENGINE=InnoDB;

CREATE OR REPLACE VIEW vw_service_request_summary AS
SELECT
    sr.request_id,
    sr.request_number,
    r.resident_code,
    CONCAT_WS(
        ' ',
        r.first_name,
        NULLIF(r.middle_name, ''),
        r.last_name,
        NULLIF(r.suffix, '')
    ) AS resident_name,
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
