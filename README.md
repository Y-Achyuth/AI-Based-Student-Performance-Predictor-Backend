# Student Performance Predictor Backend

This repository contains only the Spring Boot 3 API backend. The React frontend
is maintained and deployed from its own repository. Oracle is accessed through
Spring JDBC.

## Requirements

- Java 17 or newer
- Apache Maven 3.6.3 or newer
- Oracle Database with the application schema configured

## Configure and run

Set the Oracle connection environment variables if the defaults do not match
your local database:

```powershell
cd backend
$env:DB_URL = "jdbc:oracle:thin:@localhost:1521:xe"
$env:DB_USERNAME = "student_db"
$env:DB_PASSWORD = "<your database password>"
mvn spring-boot:run
```

Run `mvn spring-boot:run` from this repository to start the API at
<http://localhost:8080/spp/api>. This backend builds and runs independently; it
does not install or package the frontend.

To package an executable JAR, run `mvn clean package`, then start it with
`java -jar target/student-performance-predictor-1.0.0.jar` from this folder.

## Optional integrations

The ML baseline file defaults to `./data/metrics.txt`; override it with
`MODEL_FILE` if desired. SMS sending requires `TWILIO_ACCOUNT_SID`,
`TWILIO_AUTH_TOKEN`, and `TWILIO_FROM_NUMBER`. Without those values, the
messages endpoint reports that Twilio is not configured.

## API

- `GET /spp/api/students`
- `POST /spp/api/assessments`
- `POST /spp/api/attendance`
- `GET /spp/api/ml/train`
- `POST /spp/api/messages/send`

The React frontend communicates with these Spring Boot endpoints; it does not
connect directly to Oracle.

## Frontend integration

Deploy the separate frontend repository to Vercel and configure
`CORS_ALLOWED_ORIGINS` on the backend host with the exact Vercel frontend origin.
Set `VITE_API_BASE_URL` in the frontend deployment to the public backend URL
ending in `/spp/api`. Keep Oracle credentials configured only on the backend
host.
