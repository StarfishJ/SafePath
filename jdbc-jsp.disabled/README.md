# SafePath - JDBC + JSP version

This project is a minimal, non-Spring version of SafePath that uses plain JDBC (H2) and JSP/Servlets.

How to build

1. mvn package

How to run (development)

You can deploy the generated WAR (`target/jdbc-jsp-app-1.0-SNAPSHOT.war`) to any servlet container (Tomcat, Jetty) or use an IDE to run it.

Database

This project uses H2 by default and reads connection settings from `src/main/resources/application.properties`.

To create tables run the SQL in `src/main/resources/schema.sql` against the H2 database (the app will also work if tables don't exist but you should run it once).

Endpoints (basic CRUD)

All operations are handled by servlets and use POST with an `action` parameter for create/update/delete.

- GET /users -> list users (JSP)
- POST /users?action=create (params: username,email)
- POST /users?action=update (params: id,username,email)
- POST /users?action=delete (params: id)

- GET /crimeReports -> list crime reports
- POST /crimeReports?action=create (params: description,location)
  etc.

- GET /alerts -> list user alerts
- POST /alerts?action=create (params: userId,message)

Notes

This repository is intentionally minimal — no front-end forms are provided. Use HTTP clients (curl/Postman) or add simple HTML forms in the JSPs to test CRUD operations.
