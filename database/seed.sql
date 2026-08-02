Exit code: 0
Wall time: 0.4 seconds
Output:
-- BarangayConnect local-development seed data
--
-- WARNING: This script clears all rows from the application tables before seeding.
-- Run database/schema.sql first, and never use this seeder against production data.

USE barangayconnect_db;

SET time_zone = '+08:00';
SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM request_status_history;
DELETE FROM activity_logs;
DELETE FROM service_requests;
DELETE FROM users;
DELETE FROM residents;
DELETE FROM service_types;
DELETE FROM households;

SET FOREIGN_KEY_CHECKS = 1;
START TRANSACTION;

INSERT INTO households
    (household_id, household_code, address_line, purok, household_status)
VALUES
    (1, 'DEMO-HH-001', '101 Sample Street', 'Purok 1', 'ACTIVE'),
    (2, 'DEMO-HH-002', '202 Example Avenue', 'Purok 2', 'ACTIVE'),
    (3, 'DEMO-HH-003', '303 Mabini Street', 'Purok 3', 'ACTIVE');

INSERT INTO residents
    (resident_id, resident_code, household_id, first_name, middle_name, last_name, suffix,
     birth_date, sex, civil_status, contact_number, email, occupation,
     is_registered_voter, is_household_head, residency_status, date_registered)
VALUES
    (1, 'RES-2026-000001', 1, 'Juan', 'Demo', 'Dela Cruz', NULL,
     '1990-01-15', 'MALE', 'MARRIED', '09170000001', 'juan.demo@example.invalid', 'Construction Worker',
     TRUE, TRUE, 'ACTIVE', '2026-07-30'),
    (2, 'RES-2026-000002', 2, 'Maria', 'Sample', 'Santos', NULL,
     '1995-05-20', 'FEMALE', 'SINGLE', '09170000002', 'maria.sample@example.invalid', 'Vendor',
     TRUE, TRUE, 'ACTIVE', '2026-07-30'),
    (3, 'RES-2026-000003', 1, 'Ana', 'Reyes', 'Dela Cruz', NULL,
     '1992-09-08', 'FEMALE', 'MARRIED', '09170000003', 'ana.demo@example.invalid', 'Teacher',
     TRUE, FALSE, 'ACTIVE', '2026-07-30'),
    (4, 'RES-2026-000004', 3, 'Roberto', NULL, 'Garcia', 'Jr.',
     '1985-11-12', 'MALE', 'MARRIED', '09170000004', 'roberto.demo@example.invalid', 'Driver',
     FALSE, TRUE, 'ACTIVE', '2026-07-31');

-- BCrypt development hashes preserved from the original database dump.
-- Reset these passwords before using the database outside local development.
INSERT INTO users
    (user_id, resident_id, username, password_hash, display_name, role, account_status,
     failed_login_attempts, last_login_at)
VALUES
    (1, NULL, 'admin',
     '$2a$12$AuPXNroJ5.EFB2I6qaScA.g8n0ZWj278/dxNBdqB8tmCrg7..KSku',
     'System Administrator', 'ADMIN', 'ACTIVE', 0, NULL),
    (2, NULL, 'staff01',
     '$2a$12$7Yl1iCxGfrcRneaMrIAlCOzs7H9tdHGh1Ghw0RZH9KaZXKkWhqnb2',
     'Barangay Staff', 'STAFF', 'ACTIVE', 0, NULL),
    (3, 1, 'demo.resident',
     '$2a$12$7Yl1iCxGfrcRneaMrIAlCOzs7H9tdHGh1Ghw0RZH9KaZXKkWhqnb2',
     'Juan Dela Cruz', 'RESIDENT', 'ACTIVE', 0, NULL),
    (4, 2, 'maria.resident',
     '$2a$12$7Yl1iCxGfrcRneaMrIAlCOzs7H9tdHGh1Ghw0RZH9KaZXKkWhqnb2',
     'Maria Santos', 'RESIDENT', 'ACTIVE', 0, NULL);

INSERT INTO service_types
    (service_type_id, service_code, service_name, description, default_fee,
     expected_processing_days, is_active)
VALUES
    (1, 'BRGY-CLEARANCE', 'Barangay Clearance',
     'General-purpose barangay clearance request.', 50.00, 1, TRUE),
    (2, 'CERT-RESIDENCY', 'Certificate of Residency',
     'Certifies that the requester is a barangay resident.', 30.00, 1, TRUE),
    (3, 'CERT-INDIGENCY', 'Certificate of Indigency',
     'Certifies qualified indigency status for the stated purpose.', 0.00, 1, TRUE),
    (4, 'BUSINESS-CLEARANCE', 'Barangay Business Clearance',
     'Barangay-level clearance for a local business.', 100.00, 3, TRUE),
    (5, 'CERT-GOOD-MORAL', 'Certificate of Good Moral Character',
     'Certifies good standing within the barangay.', 30.00, 2, TRUE),
    (6, 'BLOTTER-FILING', 'Blotter Filing Request',
     'Requests barangay assistance in filing an incident or complaint.', 0.00, 1, TRUE);

INSERT INTO service_requests
    (request_id, request_number, resident_id, service_type_id, purpose, request_date,
     service_fee_snapshot, status, remarks, created_by_user_id, processed_by_user_id,
     processed_at, released_at, created_at, updated_at)
VALUES
    (1, 'REQ-2026-0001', 1, 1, 'Employment requirement', '2026-07-31',
     50.00, 'PENDING', NULL, 3, NULL, NULL, NULL, '2026-07-31 08:15:00', '2026-07-31 08:15:00'),
    (2, 'REQ-2026-0002', 1, 2, 'School enrollment', '2026-07-30',
     30.00, 'UNDER_REVIEW', 'Documents are being verified.', 3, 2,
     '2026-07-31 09:00:00', NULL, '2026-07-30 14:20:00', '2026-07-31 09:00:00'),
    (3, 'REQ-2026-0003', 2, 3, 'Medical assistance', '2026-07-29',
     0.00, 'APPROVED', 'Approved for certificate preparation.', 4, 2,
     '2026-07-30 10:30:00', NULL, '2026-07-29 11:10:00', '2026-07-30 10:30:00'),
    (4, 'REQ-2026-0004', 2, 1, 'Local employment application', '2026-07-25',
     50.00, 'RELEASED', 'Released to the resident.', 4, 2,
     '2026-07-26 09:45:00', '2026-07-27 15:00:00', '2026-07-25 13:05:00', '2026-07-27 15:00:00'),
    (5, 'REQ-2026-0005', 4, 4, 'New transport business application', '2026-07-28',
     100.00, 'REJECTED', 'Business address is outside the barangay.', 2, 2,
     '2026-07-29 14:00:00', NULL, '2026-07-28 10:00:00', '2026-07-29 14:00:00'),
    (6, 'REQ-2026-0006', 1, 6, 'Report repeated nighttime disturbance near our home.', '2026-08-01',
     0.00, 'UNDER_REVIEW', 'Staff will contact the resident for incident details.', 3, 2,
     '2026-08-01 09:30:00', NULL, '2026-08-01 08:10:00', '2026-08-01 09:30:00'),
    (7, 'REQ-2026-0007', 2, 6, 'Report damage to property following a neighborhood dispute.', '2026-07-30',
     0.00, 'APPROVED', 'Blotter filing intake completed.', 4, 2,
     '2026-07-30 15:20:00', NULL, '2026-07-30 13:00:00', '2026-07-30 15:20:00');

INSERT INTO request_status_history
    (history_id, request_id, old_status, new_status, remarks, changed_by_user_id, changed_at)
VALUES
    (1, 1, NULL, 'PENDING', 'Request submitted.', 3, '2026-07-31 08:15:00'),
    (2, 2, NULL, 'PENDING', 'Request submitted.', 3, '2026-07-30 14:20:00'),
    (3, 2, 'PENDING', 'UNDER_REVIEW', 'Documents are being verified.', 2, '2026-07-31 09:00:00'),
    (4, 3, NULL, 'PENDING', 'Request submitted.', 4, '2026-07-29 11:10:00'),
    (5, 3, 'PENDING', 'UNDER_REVIEW', 'Review started.', 2, '2026-07-30 09:30:00'),
    (6, 3, 'UNDER_REVIEW', 'APPROVED', 'Resident qualifies for the certificate.', 2, '2026-07-30 10:30:00'),
    (7, 4, NULL, 'PENDING', 'Request submitted.', 4, '2026-07-25 13:05:00'),
    (8, 4, 'PENDING', 'UNDER_REVIEW', 'Review started.', 2, '2026-07-26 09:00:00'),
    (9, 4, 'UNDER_REVIEW', 'APPROVED', 'Request approved.', 2, '2026-07-26 09:45:00'),
    (10, 4, 'APPROVED', 'RELEASED', 'Document released to resident.', 2, '2026-07-27 15:00:00'),
    (11, 5, NULL, 'PENDING', 'Request created by staff.', 2, '2026-07-28 10:00:00'),
    (12, 5, 'PENDING', 'UNDER_REVIEW', 'Address validation started.', 2, '2026-07-29 13:30:00'),
    (13, 5, 'UNDER_REVIEW', 'REJECTED', 'Business address is outside the barangay.', 2, '2026-07-29 14:00:00'),
    (14, 6, NULL, 'PENDING', 'Blotter filing request submitted by resident.', 3, '2026-08-01 08:10:00'),
    (15, 6, 'PENDING', 'UNDER_REVIEW', 'Staff started incident intake.', 2, '2026-08-01 09:30:00'),
    (16, 7, NULL, 'PENDING', 'Blotter filing request submitted by resident.', 4, '2026-07-30 13:00:00'),
    (17, 7, 'PENDING', 'UNDER_REVIEW', 'Staff interviewed the complainant.', 2, '2026-07-30 14:00:00'),
    (18, 7, 'UNDER_REVIEW', 'APPROVED', 'Blotter filing intake completed.', 2, '2026-07-30 15:20:00');

INSERT INTO activity_logs
    (activity_log_id, user_id, action, entity_type, entity_id, description, created_at)
VALUES
    (1, 3, 'CREATE', 'SERVICE_REQUEST', 1, 'Submitted request REQ-2026-0001.', '2026-07-31 08:15:00'),
    (2, 2, 'UPDATE_STATUS', 'SERVICE_REQUEST', 2, 'Changed request status to UNDER_REVIEW.', '2026-07-31 09:00:00'),
    (3, 2, 'UPDATE_STATUS', 'SERVICE_REQUEST', 3, 'Changed request status to APPROVED.', '2026-07-30 10:30:00'),
    (4, 2, 'UPDATE_STATUS', 'SERVICE_REQUEST', 4, 'Changed request status to RELEASED.', '2026-07-27 15:00:00'),
    (5, 2, 'UPDATE_STATUS', 'SERVICE_REQUEST', 5, 'Changed request status to REJECTED.', '2026-07-29 14:00:00'),
    (6, 1, 'CREATE', 'USER', 4, 'Created resident account maria.resident.', '2026-07-31 16:00:00'),
    (7, 1, 'UPDATE', 'SERVICE_TYPE', 4, 'Activated Barangay Business Clearance.', '2026-07-31 16:10:00'),
    (8, 2, 'VIEW_REPORT', 'REPORT', NULL, 'Viewed the service request summary.', '2026-07-31 16:30:00'),
    (9, 3, 'CREATE', 'SERVICE_REQUEST', 6, 'Submitted blotter filing request REQ-2026-0006.', '2026-08-01 08:10:00'),
    (10, 2, 'UPDATE_STATUS', 'SERVICE_REQUEST', 6, 'Started intake for blotter filing request.', '2026-08-01 09:30:00'),
    (11, 4, 'CREATE', 'SERVICE_REQUEST', 7, 'Submitted blotter filing request REQ-2026-0007.', '2026-07-30 13:00:00'),
    (12, 2, 'UPDATE', 'SERVICE_REQUEST', 7, 'Recorded intake details for blotter filing request REQ-2026-0007.', '2026-07-30 15:20:00'),
    (13, 2, 'UPDATE_STATUS', 'SERVICE_REQUEST', 7, 'Approved request after blotter intake.', '2026-07-30 15:20:00');

COMMIT;

-- Helpful verification queries:
-- SELECT role, COUNT(*) FROM users GROUP BY role;
-- SELECT status, COUNT(*) FROM service_requests GROUP BY status;
-- SELECT * FROM vw_service_request_summary ORDER BY request_id;
