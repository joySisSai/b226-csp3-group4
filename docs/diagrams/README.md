# Diagrams

## Updated Entity-Relationship Diagram

This ERD is generated from the current MySQL schema in
`src/main/resources/db/01_schema.sql`. The editable Mermaid source is
[`barangayconnect-erd.mmd`](barangayconnect-erd.mmd).

```mermaid
erDiagram
    HOUSEHOLDS {
        INT household_id PK
        VARCHAR household_code UK
        VARCHAR address_line
        VARCHAR purok "nullable"
        ENUM household_status
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    RESIDENTS {
        INT resident_id PK
        VARCHAR resident_code UK
        INT household_id FK "nullable"
        VARCHAR first_name
        VARCHAR middle_name "nullable"
        VARCHAR last_name
        VARCHAR suffix "nullable"
        DATE birth_date
        ENUM sex
        ENUM civil_status
        VARCHAR contact_number "nullable"
        VARCHAR email "nullable"
        VARCHAR occupation "nullable"
        BOOLEAN is_registered_voter
        BOOLEAN is_household_head
        ENUM residency_status
        DATE date_registered
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    USERS {
        INT user_id PK
        INT resident_id FK,UK "nullable; required for RESIDENT role"
        VARCHAR username UK
        VARCHAR password_hash
        VARCHAR display_name
        ENUM role
        ENUM account_status
        SMALLINT failed_login_attempts
        DATETIME last_login_at "nullable"
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    SERVICE_TYPES {
        SMALLINT service_type_id PK
        VARCHAR service_code UK
        VARCHAR service_name UK
        VARCHAR description "nullable"
        DECIMAL default_fee
        SMALLINT expected_processing_days
        BOOLEAN is_active
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    SERVICE_REQUESTS {
        BIGINT request_id PK
        VARCHAR request_number UK
        INT resident_id FK
        SMALLINT service_type_id FK
        VARCHAR purpose
        DATE request_date
        DECIMAL service_fee_snapshot
        ENUM status
        VARCHAR remarks "nullable"
        INT created_by_user_id FK
        INT processed_by_user_id FK "nullable"
        DATETIME processed_at "nullable"
        DATETIME released_at "nullable"
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    REQUEST_STATUS_HISTORY {
        BIGINT history_id PK
        BIGINT request_id FK
        ENUM old_status "nullable"
        ENUM new_status
        VARCHAR remarks "nullable"
        INT changed_by_user_id FK
        TIMESTAMP changed_at
    }

    ACTIVITY_LOGS {
        BIGINT activity_log_id PK
        INT user_id FK "nullable"
        VARCHAR action
        VARCHAR entity_type
        BIGINT entity_id "nullable; polymorphic reference"
        VARCHAR description "nullable"
        TIMESTAMP created_at
    }

    HOUSEHOLDS o|--o{ RESIDENTS : "contains"
    RESIDENTS o|--o| USERS : "owns resident account"
    RESIDENTS ||--o{ SERVICE_REQUESTS : "submits"
    SERVICE_TYPES ||--o{ SERVICE_REQUESTS : "classifies"
    USERS ||--o{ SERVICE_REQUESTS : "creates"
    USERS o|--o{ SERVICE_REQUESTS : "processes"
    SERVICE_REQUESTS ||--o{ REQUEST_STATUS_HISTORY : "records"
    USERS ||--o{ REQUEST_STATUS_HISTORY : "changes status"
    USERS o|--o{ ACTIVITY_LOGS : "performs"
```

### Relationship notes

- A resident may belong to one household; a household may contain many
  residents.
- A resident may have at most one user account. `users.resident_id` is nullable
  and unique: it is required for `RESIDENT` users and must be null for `STAFF`
  and `ADMIN` users.
- Every service request belongs to one resident and one service type.
- Every service request has one creator and may have one processor.
- A service request may have many status-history entries.
- Activity logs may remain after their associated user is removed because
  `activity_logs.user_id` is nullable and uses `ON DELETE SET NULL`.
- `activity_logs.entity_id` is a polymorphic application reference, not a
  database foreign key.

The `vw_service_request_summary` reporting view is intentionally not modeled as
an entity because it derives data from `service_requests`, `residents`,
`service_types`, and `users`.
