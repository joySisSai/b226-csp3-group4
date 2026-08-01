# Database setup

These scripts match the project's seven-table ERD. Resident codes are immutable application-generated values containing the current year and database ID, such as `RES-2026-000001`. Blotter filing uses the existing service-request workflow through the `BLOTTER-FILING` service type.

## Files

- `schema.sql` creates `barangayconnect_db`, recreates the seven application tables, enforces the resident-code format, and creates the service-request summary view.
- `seed.sql` clears existing rows and inserts connected local-development demo data.

> **Warning:** Both scripts reset application data. Run them only against a local database that may be erased.

## Import with phpMyAdmin

1. Open phpMyAdmin and select **Import**.
2. Import `database/schema.sql` first.
3. Import `database/seed.sql` second.

## Import from CMD

From the project directory:

```bat
mysql -u root -p < database\schema.sql
mysql -u root -p < database\seed.sql
```

Configure the project-root `.env` file afterward:

```dotenv
DB_URL=jdbc:mysql://localhost:3306/barangayconnect_db
DB_USERNAME=root
DB_PASSWORD=
```

## Seed contents

| Data | Count |
| --- | ---: |
| Households | 3 |
| Residents | 4 |
| Users | 4 |
| Service types | 6 |
| Service requests | 7 |
| Request status-history entries | 18 |
| Activity-log entries | 13 |

Seeded resident codes are `RES-2026-000001` through `RES-2026-000004`. New residents receive the current `Asia/Manila` year and a zero-padded database-generated ID.
