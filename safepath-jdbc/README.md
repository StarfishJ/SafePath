# SafePath - JDBC + JSP version

Minimal SafePath implementation using plain JDBC (H2) and Servlet/JSP (non-Spring).

## Build

- Package WAR:

```
mvn package
```

## Run (development)

- Run with Jetty (recommended):

```
mvn jetty:run
```

- Or deploy `target/jdbc-jsp-app-1.0-SNAPSHOT.war` to any servlet container (Tomcat/Jetty).

Entry points:
- Home: `/`
- Users: `/users`
- Crime Reports: `/crimeReports`
- User Alerts: `/alerts`
- H2 Console: `/h2`

## Database (H2 file)

- Config: `src/main/resources/application.properties`
- Typical JDBC URL (use an absolute path so the app and H2 Console connect to the same file):

```
jdbc:h2:file:D:/safepath_springboot/SafePath/safepath-jdbc/database/safepathdb;AUTO_SERVER=TRUE;MODE=MySQL
```

Notes:
- `AUTO_SERVER=TRUE` allows multi-process access; `MODE=MySQL` improves SQL compatibility.
- On startup, `DatabaseInitializer` checks table existence and runs `init_schema_if_not_exists.sql` only when needed (no drop, no data loss).

## H2 Console

- URL: `/h2`
- Use the same JDBC URL as the application (absolute path recommended).
- Username: `sa`; Password: empty

## Tables

- `users`: user_id, email, password_hash, created_at
- `crime_reports`: report_number, report_datetime, precinct, sector, beat, mcpp_neighborhood, blurred_address, blurred_latitude, blurred_longitude
- `user_alerts`: alert_id, user_id, radius_m, center_lat, center_lon, crime_type_filter, active_flag, created_at, updated_at

## Endpoints and features (JSP pages include forms and search)

- Users (search by email via `searchEmail`)
  - `GET /users` -> list + search
  - `GET /users?id={userId}` -> fetch one
  - `POST /users?action=create` (params: email, password)
  - `POST /users?action=update` (params: id, email, password?)
  - `POST /users?action=delete` (params: id)

- Crime Reports (search by `searchReportNumber/searchPrecinct/searchNeighborhood`)
  - `GET /crimeReports` -> list + search
  - `GET /crimeReports?reportNumber={reportNumber}` -> fetch one
  - `POST /crimeReports?action=create|update|delete`

- User Alerts (search by `searchUserId/searchAlertId`)
  - `GET /alerts` -> list + search
  - `GET /alerts?id={alertId}` -> fetch one
  - `POST /alerts?action=create|update|delete`

## Sample data

- Script: `database-manipulation/insert_mock_user_data.sql`
  - Inserts users by email if missing
  - Inserts `user_alerts` by resolving `user_id` from email (no hardcoded ids)

## Init vs reset scripts

- `src/main/resources/init_schema_if_not_exists.sql`
  - Used by the app for initialization; creates tables only if missing, does not drop data.

- `database-manipulation/create_table.sql`
  - Contains `DROP TABLE` and recreates schema. Use ONLY to fully reset the database (this will delete all data).

## Troubleshooting

- “H2 Console shows data but the app doesn’t”
  - Most likely the app and console are pointing to different DB files. Standardize on the same absolute JDBC URL.
  - Check app logs: `ConnectionManager` prints the final JDBC URL and resolved DB file path/size.

- “Table not found / data lost”
  - Ensure you didn’t run a drop script for init; verify the JDBC path in `application.properties`.

- “Foreign key error inserting user_alerts”
  - `user_id` may not be 1/2/3 (auto-increment doesn’t reset). Use the sample script that resolves `user_id` by email.

## Custom error pages

Configured in `WEB-INF/web.xml`:
- 404 -> `WEB-INF/jsp/404.jsp`
-.500 -> `WEB-INF/jsp/500.jsp`
- Exception -> `WEB-INF/jsp/error.jsp`

## Notes

- JSP pages include basic forms and search. You can also test endpoints using Postman.
