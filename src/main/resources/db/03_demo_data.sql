-- ============================================================================
-- BarangayConnect fictional demonstration data
-- Run after 02_reference_data.sql. Safe to run repeatedly.
-- Do not replace these records with real personal information.
-- ============================================================================

USE barangayconnect_db;

INSERT INTO households (
    household_code,
    address_line,
    purok,
    household_status
) VALUES
    ('DEMO-HH-001', '101 Sample Street', 'Purok Demo 1', 'ACTIVE'),
    ('DEMO-HH-002', '202 Example Avenue', 'Purok Demo 2', 'ACTIVE')
ON DUPLICATE KEY UPDATE
    address_line = VALUES(address_line),
    purok = VALUES(purok),
    household_status = VALUES(household_status);

INSERT INTO residents (
    resident_code,
    household_id,
    first_name,
    middle_name,
    last_name,
    suffix,
    birth_date,
    sex,
    civil_status,
    contact_number,
    email,
    occupation,
    is_registered_voter,
    is_household_head,
    residency_status,
    date_registered
)
SELECT
    'DEMO-RES-001',
    household_id,
    'Juan',
    'Demo',
    'Dela Cruz',
    NULL,
    '1990-01-15',
    'MALE',
    'MARRIED',
    '0000-000-0001',
    'juan.demo@example.invalid',
    'Sample Worker',
    TRUE,
    TRUE,
    'ACTIVE',
    '2026-07-30'
FROM households
WHERE household_code = 'DEMO-HH-001'
ON DUPLICATE KEY UPDATE
    household_id = VALUES(household_id),
    first_name = VALUES(first_name),
    middle_name = VALUES(middle_name),
    last_name = VALUES(last_name),
    birth_date = VALUES(birth_date),
    residency_status = VALUES(residency_status);

INSERT INTO residents (
    resident_code,
    household_id,
    first_name,
    middle_name,
    last_name,
    suffix,
    birth_date,
    sex,
    civil_status,
    contact_number,
    email,
    occupation,
    is_registered_voter,
    is_household_head,
    residency_status,
    date_registered
)
SELECT
    'DEMO-RES-002',
    household_id,
    'Maria',
    'Sample',
    'Santos',
    NULL,
    '1995-05-20',
    'FEMALE',
    'SINGLE',
    '0000-000-0002',
    'maria.sample@example.invalid',
    'Demo Vendor',
    TRUE,
    TRUE,
    'ACTIVE',
    '2026-07-30'
FROM households
WHERE household_code = 'DEMO-HH-002'
ON DUPLICATE KEY UPDATE
    household_id = VALUES(household_id),
    first_name = VALUES(first_name),
    middle_name = VALUES(middle_name),
    last_name = VALUES(last_name),
    birth_date = VALUES(birth_date),
    residency_status = VALUES(residency_status);

-- User accounts are intentionally excluded. Create the first administrator
-- through application bootstrap so its password is BCrypt-hashed.
