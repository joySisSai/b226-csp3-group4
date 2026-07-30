# Database resources

Run the scripts in numeric order:

1. `01_schema.sql` creates the database, seven core tables, constraints,
   indexes, and reporting view.
2. `02_reference_data.sql` adds required service types.
3. `03_demo_data.sql` adds fictional demonstration households and residents.
4. `99_verify.sql` checks tables, foreign keys, reference data, demo data, and
   the password-column convention.

From a MySQL client:

```powershell
mysql -u root -p < src/main/resources/db/01_schema.sql
mysql -u root -p barangayconnect_db < src/main/resources/db/02_reference_data.sql
mysql -u root -p barangayconnect_db < src/main/resources/db/03_demo_data.sql
mysql -u root -p barangayconnect_db < src/main/resources/db/99_verify.sql
```

The scripts never drop the database. Demo records are explicitly fictional,
and user accounts are excluded so passwords can be created securely by the
application.
