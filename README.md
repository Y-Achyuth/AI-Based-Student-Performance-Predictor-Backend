# Student Performance Predictor Backend

This repository contains only the Spring Boot 3 API backend. The React frontend
is maintained and deployed from its own repository. The backend uses PostgreSQL
through Spring JDBC and applies its schema migrations with Flyway.

## Requirements

- Java 17 or newer
- Apache Maven 3.6.3 or newer
- PostgreSQL (locally, or Render PostgreSQL when deployed)

## Configure and run

For local development, the defaults expect PostgreSQL at
`localhost:5432/student_performance` with the `postgres` user and password.
Override these using `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and
`SPRING_DATASOURCE_PASSWORD`, or provide a PostgreSQL connection URL through
`DATABASE_URL`.

```powershell
$env:DATABASE_URL = "postgresql://postgres:<password>@localhost:5432/student_performance"
mvn spring-boot:run
```

Run `mvn spring-boot:run` from this repository to start the API at
<http://localhost:8080/spp/api>. Flyway creates the tables automatically on
startup. The backend builds and runs independently; it does not install or
package the frontend.

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
connect directly to PostgreSQL.

## Deploy to Render

Create the database and web service separately in the Render dashboard:

1. Create a **PostgreSQL** database. Choose the region where you want to run the
   web service and note the database name.
2. Create a **Web Service** from the backend Git repository. Select **Docker**,
   leave **Root Directory** blank (the `Dockerfile` is at the repository root),
   and select the same region as the database.
3. After both resources are created, open the web service's **Environment**
   settings. Add `DATABASE_URL` and set it to the database's **Internal
   Database URL** from the Render database's **Connect** page. Do not use
   `localhost`, the external database URL, or a URL from another database.
4. Add `CORS_ALLOWED_ORIGINS` with the exact deployed frontend origin, for
   example `https://your-project.vercel.app`.
5. Set the web service health check path to `/spp/actuator/health`, save the
   environment changes, and redeploy the service.

The backend converts Render's `postgres://`/`postgresql://` `DATABASE_URL` to a
JDBC URL and enables SSL. If the service log says it is connecting to
`localhost:5432`, `DATABASE_URL` is missing or incorrect in the **web
service's** environment settings. Flyway creates the PostgreSQL schema during
startup; it does not insert student names or phone numbers. Load only authorized
student records, and keep the database URL and Twilio credentials in Render
environment settings rather than source control.

After deployment, set Vercel's `VITE_API_BASE_URL` to
`https://<your-render-service>.onrender.com/spp/api`.
For local development, use a PostgreSQL database URL and add
`http://localhost:5173` to `CORS_ALLOWED_ORIGINS`.
