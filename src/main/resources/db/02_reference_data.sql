-- ============================================================================
-- BarangayConnect required reference data
-- Run after 01_schema.sql. Safe to run repeatedly.
-- ============================================================================

USE barangayconnect_db;

INSERT INTO service_types (
    service_code,
    service_name,
    description,
    default_fee,
    expected_processing_days,
    is_active
) VALUES
    (
        'BRGY-CLEARANCE',
        'Barangay Clearance',
        'General-purpose barangay clearance request.',
        0.00,
        1,
        TRUE
    ),
    (
        'CERT-RESIDENCY',
        'Certificate of Residency',
        'Certifies that the requester is a barangay resident.',
        0.00,
        1,
        TRUE
    ),
    (
        'CERT-INDIGENCY',
        'Certificate of Indigency',
        'Certifies qualified indigency status for the stated purpose.',
        0.00,
        1,
        TRUE
    )
ON DUPLICATE KEY UPDATE
    service_name = VALUES(service_name),
    description = VALUES(description),
    default_fee = VALUES(default_fee),
    expected_processing_days = VALUES(expected_processing_days),
    is_active = VALUES(is_active);
