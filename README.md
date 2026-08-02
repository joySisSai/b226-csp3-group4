# Barangay Connect App

Console-based barangay resident, household, service-request, reporting, staff, and administrator management application.

## Requirements

- Java 21 or later
- Maven 3.9 or later
- MySQL with the `barangayconnect_db` schema installed

Verify Java and Maven from a newly opened terminal:

```text
java -version
mvn -version
```

If `mvn` is not recognized, install Maven or add its `bin` directory to the Windows `Path` environment variable. IntelliJ's bundled Maven is commonly located at:

```text
C:\Program Files\JetBrains\IntelliJ IDEA <version>\plugins\maven\lib\maven3\bin
```

Close and reopen the terminal after changing `Path`.

## Database configuration

For a new local database, import [`database/schema.sql`](database/schema.sql) first and [`database/seed.sql`](database/seed.sql) second. See [`database/README.md`](database/README.md) for setup instructions. These scripts reset application data and must only be used with a local development database.

Database credentials are not stored in source code. Each team member should copy the committed example to a local `.env` file, then update its values.

PowerShell:

```powershell
Set-Location "C:\path\to\BarangayConnectApp"
Copy-Item .env.example .env
```

Windows Command Prompt (CMD):

```bat
cd /d "C:\path\to\BarangayConnectApp"
copy .env.example .env
```

Bash-compatible terminal (Git Bash, Linux, or macOS):

```bash
cd /path/to/BarangayConnectApp
cp .env.example .env
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

In IntelliJ, open **Run -> Edit Configurations**, enable **EnvFile**, and select `$PROJECT_DIR$/.env`. This loads the `.env` values as environment variables.

## Running

### IntelliJ IDEA

After configuring EnvFile, run `com.joysistvi.brgyconnectapp.BarangayConnectApplication`.

### PowerShell terminal

Open PowerShell, navigate to the project directory containing `pom.xml`, load `.env` into the current process, and run the application:

```powershell
Set-Location "C:\path\to\BarangayConnectApp"
Get-Content .env | ForEach-Object {
    if ($_ -match '^\s*([^#][^=]*)=(.*)$') {
        [Environment]::SetEnvironmentVariable($matches[1].Trim(), $matches[2], 'Process')
    }
}
mvn compile exec:java "-Dexec.mainClass=com.joysistvi.brgyconnectapp.BarangayConnectApplication"
```

### Windows Command Prompt (CMD)

Open CMD, navigate to the project directory containing `pom.xml`, load `.env` into the current session, and run the application:

```bat
cd /d "C:\path\to\BarangayConnectApp"
for /f "usebackq eol=# tokens=1,* delims==" %A in (".env") do @set "%A=%B"
mvn compile exec:java -Dexec.mainClass=com.joysistvi.brgyconnectapp.BarangayConnectApplication
```

When placing the CMD command in a `.bat` file, use `%%A` and `%%B` instead of `%A` and `%B`.

### Running the Executable JAR

Once built using `mvn clean package`, the application produces an executable "fat JAR" containing all required dependencies. You can run it directly with Java:

```powershell
java -jar target/BarangayConnectApp-1.0-SNAPSHOT.jar
```
*(Remember to load `.env` into your environment first, as shown in the previous steps).*

At startup, the application validates its configuration and database connection. It exits with a safe diagnostic if required configuration is missing or MySQL is unavailable.

## Build verification and Testing

Run the build and tests from the project directory containing `pom.xml`.

PowerShell:

```powershell
Set-Location "C:\path\to\BarangayConnectApp"
mvn clean package
```

Windows Command Prompt (CMD):

```bat
cd /d "C:\path\to\BarangayConnectApp"
mvn clean package
```

A successful build runs the automated JUnit 5 test suite and creates `target/BarangayConnectApp-1.0-SNAPSHOT.jar`. If Maven reports that no POM was found, check that the current directory contains `pom.xml` before running the command.
