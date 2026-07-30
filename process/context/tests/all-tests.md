# BarangayConnect Test Context

These are the authoritative Phase 01 commands.

## Automated verification

```powershell
mvn clean test
mvn clean verify
```

Both commands must complete successfully. `MainSmokeTest` confirms that the
starter prints `BarangayConnect` and returns normally.

## Manual smoke test

```powershell
mvn -q compile
java -cp target/classes com.barangayconnect.Main
```

Expected output: `BarangayConnect`

## Repository checks

```powershell
git status --short
git ls-files
git check-ignore target
```

Confirm that IDE output, logs, and `target/` are not tracked. A complete
clean-folder build remains required before Phase 01 can be marked verified.
