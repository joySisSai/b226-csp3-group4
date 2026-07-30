# BarangayConnect Project Context

## Goal

Build a teachable Java CLI system for barangay resident records and service
request management using layered MVC and plain JDBC.

## Version 1 scope

- Actors: Resident, Staff, and Administrator
- Resident self-service: log in, view own profile, submit and track requests,
  cancel a pending request, and log out
- Staff and administrator workflows for records, request processing, reporting,
  and audit
- MySQL persistence with seven planned core tables

Web/mobile UI, messaging, payments, biometrics, government integrations, and
multi-barangay tenancy are out of scope.

## Architecture

`View -> Controller -> Service -> Repository -> MySQL`

Models carry entities, DTOs, and enums between layers. Views and controllers do
not execute SQL. Services and repositories do not read console input.

## Data and security

- Roles: `RESIDENT`, `STAFF`, `ADMIN`
- Request states: `PENDING`, `UNDER_REVIEW`, `APPROVED`, `RELEASED`,
  `REJECTED`, `CANCELLED`
- Planned core tables: households, residents, users, service_types,
  service_requests, request_status_history, activity_logs
- Resident accounts have a nullable unique resident link and require staff
  activation.
- Use fictional development data only. Never commit credentials or personal
  information.
- Preserve historical records through status changes and append-only history.

## Current phase

Phase 01 establishes the repository and testable project foundation. Database
schema and JDBC implementation begin in Phase 02.

## Workflow

Keep `main` stable, use short-lived `feature/<card-name>` branches, review pull
requests, and format commits as `type(scope): summary`.
