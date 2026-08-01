# Barangay Connect App

Console-based barangay resident, household, service-request, reporting, staff, and administrator management application.

## Requirements

- Java 21 or later
- Maven 3.9 or later
- MySQL with the `barangayconnect_db` schema installed

## Database configuration

Database credentials are not stored in source code. Each team member should copy the committed example and update their local `.env` file:

```powershell
Copy-Item .env.example .env
```

The `.env` file uses these settings:

| Variable | Required | Example |
| --- | --- | --- |
| `DB_URL` | Yes | `jdbc:mysql://localhost:3306/barangayconnect_db` |
| `DB_USERNAME` | Yes | `root` |
| `DB_PASSWORD` | No | Your MySQL password; omit it only for a blank password |

Example `.env`:

```dotenv
DB_URL=jdbc:mysql://localhost:3306/barangayconnect_db
DB_USERNAME=root
DB_PASSWORD=replace-with-your-password
```

For a local MySQL account with a blank password, leave `DB_PASSWORD=` empty.

In IntelliJ, open **Run → Edit Configurations**, enable **EnvFile**, and select `$PROJECT_DIR$/.env`. The EnvFile integration exposes those values as ordinary environment variables to the application.

Java system properties and operating-system environment variables are also supported. Precedence is:

1. Java system properties
2. Operating-system environment variables

Supported Java system properties:

- `barangayconnect.db.url`
- `barangayconnect.db.username`
- `barangayconnect.db.password`

## Running

Run `com.joysistvi.brgyconnectapp.BarangayConnectApplication` from the IDE after selecting the local `.env` under **Run → Edit Configurations → EnvFile**.

At startup, the application validates its configuration and database connection. It exits with a safe diagnostic if required configuration is missing or MySQL is unavailable.

## Build verification

```powershell
mvn clean package
```

No JUnit or Mockito dependencies are required by this project.
