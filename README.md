# Electronic Child Health Records (ECHR) API

A REST API for managing personal healthcare records for children, built with Java 17, Spring Boot 3.2, and MySQL.

## Tech Stack

- **Java 17**
- **Spring Boot 3.2.5** — Web, Data JPA, Security, Validation, Actuator
- **MySQL 8** — persistence
- **Liquibase** — schema migrations (run on startup)
- **JWT (jjwt 0.12.5)** — stateless authentication
- **AES-256-GCM** — field-level encryption for sensitive data at rest
- **springdoc-openapi 2.0.4** — Swagger UI and OpenAPI docs
- **Lombok** — boilerplate reduction

---

## Project Structure

```
src/main/java/com/healthcare/childrecords/
├── ChildHealthcareApplication.java
├── config/
│   ├── CorsConfig.java
│   ├── OpenApiConfig.java
│   └── SecurityConfig.java
├── controller/          # 13 REST controllers (see API Endpoints)
├── dto/
│   ├── request/         # Request bodies, e.g. ChildRequest, AppointmentRequest,
│   │                    # MedicalRecordRequest, VaccinationRequest, GrowthMeasurementRequest,
│   │                    # DoctorChildConnectionRequest, ChangePasswordRequest, ...
│   └── response/        # Response bodies, e.g. ChildResponse, AppointmentResponse,
│                        # GrowthAlertResponse, VaccinationScheduleResponse, ...
├── entity/
│   ├── User.java, Doctor.java, Parent.java, Child.java
│   ├── Appointment.java, MedicalRecord.java, Vaccination.java, Allergy.java
│   ├── BirthRecord.java, GrowthMeasurement.java, GrowthAlertRule.java,
│   │   GrowthAlertAcknowledgement.java, GrowthReference.java
│   ├── DoctorChildRequest.java
│   ├── Vaccine.java, VaccineCombination.java, VaccinationSchedule.java
│   └── enums: Role, Gender, AppointmentStatus, AllergySeverity, DeliveryType,
│              MeasurementType, GrowthAlertType
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   └── AccessDeniedException.java
├── repository/          # Spring Data JPA repositories
├── security/
│   ├── JwtUtil.java
│   ├── JwtAuthFilter.java
│   └── UserDetailsServiceImpl.java
├── service/             # Business logic; ChildAccessGuard centralises child-data access checks
└── util/
    ├── EncryptionUtil.java       # AES-256-GCM encrypt/decrypt
    ├── EncryptionConverter.java  # JPA AttributeConverter
    └── AmkaHashUtil.java         # SHA-256 hash of AMKA, used for lookups
```

Database migrations live in `src/main/resources/db/changelog/` (Liquibase, `001` – `033`).

---

## Entities and Relationships

```
Parent (1) ──────── (N) Child (N) ──────── (M) Doctor
                          │                    (join table: child_doctor)
              ┌───────────┼───────────┬──────────────┬──────────────┬──────────────┐
              │           │           │              │              │              │
         Appointment  MedicalRecord  Vaccination  Allergy   GrowthMeasurement   BirthRecord
              │
          MedicalRecord (optional 1:1 link via appointment_id)
```

| Relationship | Cardinality |
|---|---|
| Parent → Children | One-to-Many |
| Child ↔ Doctor | Many-to-Many (`child_doctor` join table) |
| Doctor ↔ Child (connection requests) | Tracked in `doctor_child_requests` until a parent approves or rejects |
| Child → Appointments | One-to-Many |
| Child → MedicalRecords | One-to-Many |
| Child → Vaccinations | One-to-Many |
| Child → Allergies | One-to-Many |
| Child → Growth measurements, growth alert acknowledgements, birth record | One-to-Many / One-to-One (see entities) |
| Appointment → MedicalRecord | One-to-One (optional; `MedicalRecord` holds the `appointment_id` foreign key) |

---

## Authentication & Authorisation

Authentication is JWT-based (Bearer token). There are two roles, defined in the `Role` enum as `ROLE_DOCTOR` and `ROLE_PARENT`.

| Role | Permissions |
|---|---|
| `ROLE_PARENT` | Register and manage their own children, assign doctors: approve or reject doctor connection requests, read all health data for their children, create/cancel appointments |
| `ROLE_DOCTOR` | Send connection requests to children, read data for assigned children only, create/update medical records, vaccinations, growth measurements, birth records, allergies, and appointments |

Child-data access is enforced centrally in `ChildAccessGuard`:

- `verifyAccess(username, child)` — allows the owning parent, or a doctor assigned to the child. Used by all read and clinical write operations on child-scoped data.
- `verifyParentOwnership(username, child)` — allows only the owning parent. Used for administrative actions such as updating or deleting a child and assigning doctors.

Two further rules are enforced in the services:

- A doctor can update a medical record only if they created it.
- A doctor can create an appointment only for a child they are connected to.

### Auth Endpoints

| Method | Path | Access | Description |
|---|---|---|---|
| POST | `/api/auth/register/doctor` | Public | Register a doctor account |
| POST | `/api/auth/register/parent` | Public | Register a parent account |
| POST | `/api/auth/login` | Public | Obtain a JWT token |
| POST | `/api/auth/change-password` | Authenticated | Change the current user's password |

All other endpoints require `Authorization: Bearer <token>`.

---

## API Endpoints

### Doctors — `/api/doctors`

| Method | Path | Access | Description |
|---|---|---|---|
| GET | `/api/doctors` | Doctor, Parent | List all doctors |
| GET | `/api/doctors/{id}` | Doctor, Parent | Get doctor by ID |
| GET | `/api/doctors/me` | Doctor | Get own profile |
| PATCH | `/api/doctors/me` | Doctor | Update own profile |
| GET | `/api/doctors/search/amka` | Doctor, Parent | Find a doctor by AMKA |
| GET | `/api/doctors/search/name` | Doctor, Parent | Find doctors by name (partial match) |

### Parents — `/api/parents`

| Method | Path | Access | Description |
|---|---|---|---|
| GET | `/api/parents/me` | Parent | Get own profile |
| PATCH | `/api/parents/me` | Parent | Update own profile |

### Children — `/api/children`

| Method | Path | Access | Description |
|---|---|---|---|
| POST | `/api/children` | Parent | Add a child |
| GET | `/api/children` | Parent, Doctor | List own children (parent) or assigned children (doctor) |
| GET | `/api/children/search?amka=` | Auth | Find a child by AMKA |
| GET | `/api/children/{id}` | Parent (owner), Doctor (assigned) | Get child by ID |
| PUT | `/api/children/{id}` | Parent (owner) | Update child details |
| DELETE | `/api/children/{id}` | Parent (owner) | Remove a child |
| GET | `/api/children/{id}/doctors` | Parent (owner), Doctor (assigned) | List doctors connected to a child |
| POST | `/api/children/{childId}/doctors/{doctorId}` | Parent (owner) | Assign a doctor directly |
| DELETE | `/api/children/{childId}/doctors/{doctorId}` | Parent (owner) | Remove a doctor |

### Doctor–Child Connection Requests — `/api/doctor-child-requests`

A doctor can request a connection to a child. The parent then approves or rejects it.

| Method | Path | Access | Description |
|---|---|---|---|
| POST | `/api/doctor-child-requests` | Doctor | Send a connection request for a child |
| GET | `/api/doctor-child-requests/pending` | Parent | List pending requests for the parent's children |
| GET | `/api/doctor-child-requests/pending/sent` | Doctor | List the doctor's own pending requests |
| POST | `/api/doctor-child-requests/{requestId}/approve` | Parent | Approve a request (connects the doctor to the child) |
| POST | `/api/doctor-child-requests/{requestId}/reject` | Parent | Reject a request |

### Appointments — `/api/appointments`

| Method | Path | Access | Description |
|---|---|---|---|
| POST | `/api/appointments` | Parent, Doctor | Create appointment |
| GET | `/api/appointments` | Parent, Doctor | List own appointments |
| GET | `/api/appointments/child/{childId}` | Parent, Doctor | Appointments for a child |
| PATCH | `/api/appointments/{id}/status` | Parent, Doctor | Update status (SCHEDULED / COMPLETED / CANCELLED) |

### Medical Records — `/api/medical-records`

| Method | Path | Access | Description |
|---|---|---|---|
| POST | `/api/medical-records` | Doctor | Create a record |
| GET | `/api/medical-records/child/{childId}` | Parent, Doctor | Records for a child |
| GET | `/api/medical-records/{id}` | Parent, Doctor | Get record by ID |
| PUT | `/api/medical-records/{id}` | Doctor (creator only) | Update a record |

### Vaccinations — `/api/vaccinations`

| Method | Path | Access | Description |
|---|---|---|---|
| POST | `/api/vaccinations` | Doctor | Log a vaccination |
| GET | `/api/vaccinations/child/{childId}` | Parent, Doctor | Vaccinations for a child |
| GET | `/api/vaccinations/{id}` | Parent, Doctor | Get vaccination by ID |
| PUT | `/api/vaccinations/{id}` | Doctor | Update a vaccination |

### Allergies — `/api/allergies`

| Method | Path | Access | Description |
|---|---|---|---|
| POST | `/api/allergies` | Doctor | Record an allergy |
| GET | `/api/allergies/child/{childId}` | Parent, Doctor | Allergies for a child |
| GET | `/api/allergies/{id}` | Parent, Doctor | Get allergy by ID |
| PUT | `/api/allergies/{id}` | Doctor | Update an allergy |
| DELETE | `/api/allergies/{id}` | Doctor | Delete an allergy |

### Birth Records — `/api/birth-records`

| Method | Path | Access | Description |
|---|---|---|---|
| POST | `/api/birth-records` | Doctor | Create a birth record |
| GET | `/api/birth-records/child/{childId}` | Parent, Doctor | Birth record for a child (`204` if none yet) |
| PUT | `/api/birth-records/child/{childId}` | Doctor | Update a child's birth record |

### Growth Measurements — `/api/growth-measurements`

| Method | Path | Access | Description |
|---|---|---|---|
| POST | `/api/growth-measurements` | Doctor | Record a measurement |
| GET | `/api/growth-measurements/child/{childId}` | Parent, Doctor | Measurements for a child |
| GET | `/api/growth-measurements/{id}` | Parent, Doctor | Get measurement by ID |
| PUT | `/api/growth-measurements/{id}` | Doctor | Update a measurement |
| DELETE | `/api/growth-measurements/{id}` | Doctor | Delete a measurement |
| GET | `/api/growth-measurements/child/{childId}/alerts` | Doctor | Growth alerts for a child |
| POST | `/api/growth-measurements/child/{childId}/alerts/{alertKey}/acknowledge` | Doctor | Acknowledge a growth alert |
| GET | `/api/growth-measurements/references/child/{childId}/type/{measurementType}` | Parent, Doctor | WHO reference curves for a child and measurement type |

### Vaccines and Vaccination Schedules — `/api/vaccines`, `/api/vaccination-schedules`

| Method | Path | Access | Description |
|---|---|---|---|
| GET | `/api/vaccines` | Doctor, Parent | List vaccines |
| GET | `/api/vaccination-schedules` | Doctor, Parent | List vaccination schedules |
| GET | `/api/vaccination-schedules/{id}` | Doctor, Parent | Get schedule by ID |
| GET | `/api/vaccination-schedules/vaccine/{vaccineName}` | Doctor, Parent | Schedules for a vaccine |
| GET | `/api/vaccination-schedules/age/{ageMonths}` | Doctor, Parent | Schedules applicable at an age in months |

---

## Sensitive Data Encryption

Personally identifiable and medically sensitive fields are encrypted in the database using **AES-256-GCM** with a random 96-bit IV per value and a 128-bit authentication tag. The stored value is the base64 encoding of `IV || ciphertext`, kept in `TEXT` columns.

Encryption is transparent to the application via the JPA `AttributeConverter` (`EncryptionConverter`), which wraps `EncryptionUtil`.

Because encrypted values cannot be searched directly, AMKA lookups use a SHA-256 hash (`AmkaHashUtil`) stored alongside the encrypted value.

### Encrypted fields

| Entity | Encrypted fields |
|---|---|
| Doctor | `firstName`, `lastName`, `amka`, `email`, `phoneNumber`, `licenseNumber` |
| Parent | `firstName`, `lastName`, `email`, `phoneNumber`, `address` |
| Child | `firstName`, `lastName`, `amka`, `dateOfBirth`, `bloodType` |
| Appointment | `reason`, `notes` |
| MedicalRecord | `diagnosis`, `notes`, `prescription` |
| Vaccination | `batchNumber`, `notes` |
| Allergy | `allergen`, `reaction`, `notes` |
| BirthRecord | `birthHospital`, `notes` |
| GrowthMeasurement | `notes` |
| GrowthAlertAcknowledgement | `note` |

---

## Configuration

Settings are in `src/main/resources/application.properties`. Secrets are read from environment variables, with the values below as defaults where one is given:

```properties
spring.datasource.url=jdbc:mysql://localhost:3307/child_healthcare_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:root}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:}

app.jwt.secret=${APP_JWT_SECRET:...}          # at least 32 characters
app.jwt.expiration-ms=86400000

# Base64-encoded 256-bit AES key. Generate with: openssl rand -base64 32
app.encryption.key=${APP_ENCRYPTION_KEY:}
```

Other notable settings:

- `spring.jpa.hibernate.ddl-auto=validate` — Liquibase creates and migrates the schema; Hibernate only validates it.
- Profiles `application-docker.properties` and `application-prod.properties` are provided for container and production deployments.

### Docker Compose

`docker-compose.yml` starts MySQL 8 (port 3307) and the application (port 8080). It reads these variables from a `.env` file in the project root:

```
MYSQL_ROOT_PASSWORD=
MYSQL_DATABASE=
DB_USER=
DB_PASSWORD=
JWT_SECRET=
ENCRYPTION_KEY=
```

`.env` contains local credentials and must not be committed.

---

## Running the Application

```bash
# Build
./mvnw clean package

# Run locally (requires a MySQL instance on port 3307 and the environment variables above)
./mvnw spring-boot:run

# Run tests
./mvnw test

# Run with Docker Compose (MySQL + application)
docker compose up --build
```

The API is available at `http://localhost:8080`.

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health check: `http://localhost:8080/actuator/health`

---
