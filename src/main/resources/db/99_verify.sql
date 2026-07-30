-- ============================================================================
-- BarangayConnect database verification
-- Run after the schema and data scripts.
-- ============================================================================

USE barangayconnect_db;

SELECT 'BarangayConnect database setup completed.' AS setup_result;

SELECT TABLE_NAME
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'barangayconnect_db'
  AND TABLE_TYPE = 'BASE TABLE'
ORDER BY TABLE_NAME;

SELECT COUNT(*) AS core_table_count
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'barangayconnect_db'
  AND TABLE_TYPE = 'BASE TABLE'
  AND TABLE_NAME IN (
      'households',
      'residents',
      'users',
      'service_types',
      'service_requests',
      'request_status_history',
      'activity_logs'
  );

SELECT TABLE_NAME, CONSTRAINT_NAME
FROM information_schema.TABLE_CONSTRAINTS
WHERE CONSTRAINT_SCHEMA = 'barangayconnect_db'
  AND CONSTRAINT_TYPE = 'FOREIGN KEY'
ORDER BY TABLE_NAME, CONSTRAINT_NAME;

SELECT service_code, service_name, is_active
FROM barangayconnect_db.service_types
ORDER BY service_type_id;

SELECT resident_code, first_name, last_name, residency_status
FROM barangayconnect_db.residents
WHERE resident_code LIKE 'DEMO-%'
ORDER BY resident_code;

SELECT COUNT(*) AS plain_text_password_column_count
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'barangayconnect_db'
  AND TABLE_NAME = 'users'
  AND COLUMN_NAME = 'password';
