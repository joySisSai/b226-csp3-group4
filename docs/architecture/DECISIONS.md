# BarangayConnect Architecture Decisions

## Foundation decisions

1. **Java 17 and Maven.** Java 17 is the compatibility target. Maven commands
   are authoritative for build, test, verification, and packaging.
2. **Layered CLI MVC.** Dependencies flow from View to Controller to Service to
   Repository to MySQL, with Model objects shared between layers.
3. **MySQL 8.0+ and plain JDBC.** Repositories own prepared SQL and row mapping.
   Services own transactions and business rules.
4. **Three roles.** The system supports `RESIDENT`, `STAFF`, and `ADMIN`.
   Resident accounts link one-to-one to a resident record and require staff
   activation.
5. **Traceable request state.** Request states are `PENDING`, `UNDER_REVIEW`,
   `APPROVED`, `RELEASED`, `REJECTED`, and `CANCELLED`. Status history and
   security-relevant audit events are append-only.
6. **Soft deactivation.** Historical records are preserved by status changes
   instead of destructive deletion.
7. **Security baseline.** Passwords are BCrypt hashes, SQL is parameterized,
   login errors are generic, resident access requires ownership checks, and
   secrets or real resident data are never committed.

## Layer rules

- Views perform console input/output only.
- Controllers coordinate use cases and navigation; they contain no SQL.
- Services perform validation, authorization, reporting, password handling,
  and transaction coordination.
- Repositories contain JDBC persistence and row mapping.
- Models contain entities, DTOs, and enums only.
