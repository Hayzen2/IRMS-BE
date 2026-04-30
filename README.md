# IRMS Backend

IRMS is a Spring Boot backend for an integrated restaurant management system. The codebase is organized into two main business modules: Admin Tools and Order Fulfillment. Order Fulfillment includes both digital ordering and kitchen coordination.

## Tech Stack

- Java 21
- Spring Boot 4
- Spring Web MVC
- Spring Security
- Spring Data JPA
- PostgreSQL
- JWT authentication
- WebSocket support
- SpringDoc OpenAPI / Swagger UI
- Maven

## Project Structure

### Business Modules

1. Admin Tools
	- Manages internal administration workflows
	- Located under `src/main/java/com/example/IRMS/modules/admin_tools/`

2. Order Fulfillment
	- Handles customer ordering and kitchen workflow
	- Located under `src/main/java/com/example/IRMS/modules/digital_ordering/`
	- Kitchen coordination is implemented under `src/main/java/com/example/IRMS/modules/kitchen_coordination/`

- `src/main/java/com/example/IRMS/IrmsApplication.java` - application entry point
- `src/main/java/com/example/IRMS/config/` - security, JWT, CORS, WebSocket, and OpenAPI configuration
- `src/main/java/com/example/IRMS/modules/` - business modules grouped by domain
- `src/main/java/com/example/IRMS/utils/` - shared utilities and startup data initialization
- `src/main/resources/application.yaml` - application configuration
- `compose.yaml` - local PostgreSQL and pgAdmin stack
- `.env` - local environment values loaded at startup

## Prerequisites

- Java 21 installed and available on your PATH
- Maven 3.9+ or the included Maven Wrapper
- PostgreSQL 16 if you want to run the app without Docker Compose
- Docker and Docker Compose if you want the local database stack

## Configuration

The application loads a `.env` file from the project root before Spring starts. The main configuration values used by the app are:

- `SERVER_PORT` - Spring Boot server port
- `POSTGRES_HOST` - Database host
- `POSTGRES_PORT` - Database port
- `POSTGRES_DB` - Database name
- `POSTGRES_USER` - Database user
- `POSTGRES_PASSWORD` - Database password
- `JWT_SECRET` - JWT signing secret
- `FRONTEND_URL` - CORS origin for the frontend
- `PGADMIN_EMAIL` - pgAdmin login email
- `PGADMIN_PASSWORD` - pgAdmin login password
- `PGADMIN_PORT` - pgAdmin port

The application reads these from `.env` in `src/main/resources/application.yaml` and constructs the JDBC URL as `jdbc:postgresql://${POSTGRES_HOST}:${POSTGRES_PORT}/${POSTGRES_DB}`.

Example:

```env
SERVER_PORT=8080

# PostgreSQL Database
POSTGRES_USER=postgres
POSTGRES_PASSWORD=Happyday@
POSTGRES_DB=irms_db
POSTGRES_HOST=localhost
POSTGRES_PORT=5432

PGADMIN_EMAIL=admin@local.dev
PGADMIN_PASSWORD=pgadmin123
PGADMIN_PORT=5050

FRONTEND_URL=http://localhost:5173
JWT_SECRET=your_jwt_secret_key
```

## Quick Start (Full Tutorial)

Follow these steps to get the full IRMS backend running with Docker, pgAdmin, and automatic data initialization.

### Step 1: Clean Up Old Containers (if needed)

If you have old `postgres` or `pgadmin` containers, remove them first:

```bash
docker stop postgres-irms pgadmin-irms
docker rm postgres-irms pgadmin-irms
```

### Step 2: Start Docker Containers

Start PostgreSQL and pgAdmin using Docker Compose:

```bash
docker compose up -d
```

This will:
- Start **PostgreSQL 16** on `localhost:5434`
- Start **pgAdmin** on `localhost:5050`
- Create the `irms_db` database automatically
- Create Docker volumes for persistent data

**Verify containers are running:**

```bash
docker ps
```

You should see `postgres-irms` and `pgadmin-irms` containers listed.

### Step 3: Register pgAdmin Connection

1. Open pgAdmin in your browser: `http://localhost:5050`

2. Login with credentials from `.env`:
   - Email: `admin@local.dev`
   - Password: `pgadmin123`

3. Register a new server:
   - Right-click **Servers** → **Register** → **Server**
   - **General tab:**
     - Name: `IRMS PostgreSQL`
   - **Connection tab:**
     - Host name/address: `localhost`
     - Port: `5434`
     - Username: `postgres` (from `.env`)
     - Password: `Happyday@` (from `.env`)
     - Database: `irms_db`
   - Click **Save**

4. Verify the connection appears under **Servers** in the left sidebar

### Step 4: Run the Spring Boot Backend

In a new terminal, start the application:

**On Windows:**
```bash
mvnw.cmd spring-boot:run
```

**On macOS or Linux:**
```bash
./mvnw spring-boot:run
```

**Or if Maven is installed:**
```bash
mvn spring-boot:run
```

Wait for the startup log to show:
```
Started IrmsApplication in X.XXX seconds
```

### Step 5: Data Initialization (Automatic)

When the application starts, it automatically:
- Reads the `.env` file for database credentials
- Connects to PostgreSQL via the JDBC URL built from `POSTGRES_HOST`, `POSTGRES_PORT`, and `POSTGRES_DB`
- Runs Hibernate's `ddl-auto: update` to create or update tables
- Executes `DataInitializer.java` to seed initial data (admin users, roles, etc.)

**Check the logs for:**
```
[INFO] Processing PersistenceUnitInfo
[INFO] Hibernate: CREATE TABLE ...
[INFO] DataInitializer: Initializing sample data
```

### Step 6: Access the Application

The backend is now ready:

- **API Base:** `http://localhost:8080`
- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI Docs:** `http://localhost:8080/v3/api-docs`
- **pgAdmin:** `http://localhost:5050`

## Seeded Test Accounts

`DataInitializer.java` seeds one account for each role at startup (if the email does not already exist). You can use these for role-based endpoint testing:

- **Manager**
  - Email: `manager@irms.com`
  - Password: `manager123`
- **Chef**
  - Email: `chef@irms.com`
  - Password: `chef123`
- **Server**
  - Email: `server@irms.com`
  - Password: `server123`
- **Cashier**
  - Email: `cashier@irms.com`
  - Password: `cashier123`

These credentials are for local development/testing only.

## Database Setup

The application is configured to use JPA with `ddl-auto: update`, so Hibernate will create or update tables on startup.

**How it works:**
1. Docker Compose runs `init-db.sql` on PostgreSQL startup, which creates the `irms_db` database
2. Spring Boot connects via the JDBC URL constructed from `.env` variables
3. Hibernate creates/updates all tables automatically
4. `DataInitializer.java` populates seed data (admin users, roles, initial records)

No manual database setup is required if you use Docker Compose.

## Testing

Run the test suite with:

```bash
mvn test
```

If you prefer the wrapper:

```bash
./mvnw test
```

## Common Commands

- Run the application: `mvn spring-boot:run`
- Build the project: `mvn clean package`
- Run tests: `mvn test`
- Clean build output: `mvn clean`

## Troubleshooting

- If startup fails with `'url' must start with "jdbc"`, check that `.env` contains valid `POSTGRES_HOST`, `POSTGRES_PORT`, and `POSTGRES_DB` values. The app constructs the JDBC URL from these at runtime.
- If the app cannot connect to the database, confirm that PostgreSQL is running on the host/port specified in `.env` and that `POSTGRES_USER` / `POSTGRES_PASSWORD` match your database credentials.
- If Swagger UI does not load, verify the app started on port `8080` and that no other service is using the port.
- If CORS issues appear in the frontend, confirm `FRONTEND_URL` in `.env` matches the actual frontend origin.
- If pgAdmin cannot connect, verify `POSTGRES_HOST` is reachable and `POSTGRES_DB` exists after the postgres service starts.

## Notes

- The application entry point loads `.env` manually before Spring initializes.
- The project uses package-based modules for different restaurant workflows rather than a single monolithic service layer.
- The two main business areas are Admin Tools and Order Fulfillment, with kitchen coordination kept alongside ordering as part of the fulfillment flow.
- `HELP.md` contains the default Spring Initializr reference material, but this README is the project-specific setup guide.
