# Child Healthcare Records API

A REST API for managing personal healthcare records for children, built with Java 17, Spring Boot 3.2, and MySQL.

## Tech Stack

- **Java 17**
- **Spring Boot 3.2.5** — Web, Data JPA, Security, Validation
- **MySQL 8** — persistence
- **JWT (jjwt 0.12.5)** — stateless authentication
- **AES-256-GCM** — field-level encryption for sensitive data at rest
- **Lombok** — boilerplate reduction

---

## Project Structure

```
src/main/java/com/healthcare/childrecords/
├── ChildHealthcareApplication.java
├── config/
│   └── SecurityConfig.java
├── controller/
│   ├── AuthController.java
│   ├── DoctorController.java
│   ├── ParentController.java
│   ├── ChildController.java
│   ├── AppointmentController.java
│   ├── MedicalRecordController.java
│   ├── VaccinationController.java
│   └── AllergyController.java
├── dto/
│   ├── request/         # LoginRequest, RegisterDoctorRequest, RegisterParentRequest,
│   │                    # ChildRequest, AppointmentRequest, MedicalRecordRequest,
│   │                    # VaccinationRequest, AllergyRequest
│   └── response/        # AuthResponse, DoctorResponse, ParentResponse, ChildResponse,
│                        # AppointmentResponse, MedicalRecordResponse,
│                        # VaccinationResponse, AllergyResponse
├── entity/
│   ├── User.java
│   ├── Doctor.java
│   ├── Parent.java
│   ├── Child.java
│   ├── Appointment.java
│   ├── MedicalRecord.java
│   ├── Vaccination.java
│   ├── Allergy.java
│   └── enums: Role, Gender, AppointmentStatus, AllergySeverity
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   └── AccessDeniedException.java
├── repository/          # JPA repositories for all 8 entities
├── security/
│   ├── JwtUtil.java
│   ├── JwtAuthFilter.java
│   └── UserDetailsServiceImpl.java
├── service/             # Business logic for all 8 entities
└── util/
    ├── EncryptionUtil.java       # AES-256-GCM encrypt/decrypt
    └── EncryptionConverter.java  # JPA AttributeConverter
```

---

## Entities and Relationships

```
Parent (1) ──────── (N) Child (N) ──────── (M) Doctor
                          │
              ┌───────────┼───────────┬──────────────┐
              │           │           │              │
         Appointment  MedicalRecord  Vaccination  Allergy
              │
          MedicalRecord (1:1 optional link)
```

| Relationship | Cardinality |
|---|---|
| Parent → Children | One-to-Many |
| Child ↔ Doctor | Many-to-Many (`child_doctor` join table) |
| Child → Appointments | One-to-Many |
| Child → MedicalRecords | One-to-Many |
| Child → Vaccinations | One-to-Many |
| Child → Allergies | One-to-Many |
| Appointment → MedicalRecord | One-to-One (optional) |

---

## Authentication & Authorisation

Authentication is JWT-based (Bearer token). There are two roles:

| Role | Permissions |
|---|---|
| `ROLE_PARENT` | Register/manage their own children; assign doctors to children; read all health data for their children; create/cancel appointments |
| `ROLE_DOCTOR` | Read data for assigned children only; create/update medical records, vaccinations, appointments |

Access control is enforced centrally in `ChildService.verifyAccess()` — every child-scoped operation passes through this check before any data is returned or modified.

### Auth Endpoints (public)

| Method | Path | Description |
|---|---|---|
| POST | `/api/auth/register/doctor` | Register a doctor account |
| POST | `/api/auth/register/parent` | Register a parent account |
| POST | `/api/auth/login` | Obtain a JWT token |

All other endpoints require `Authorization: Bearer <token>`.

---

## API Endpoints

### Doctors — `/api/doctors`

| Method | Path | Roles | Description |
|---|---|---|---|
| GET | `/api/doctors` | DOCTOR, PARENT | List all doctors |
| GET | `/api/doctors/{id}` | DOCTOR, PARENT | Get doctor by ID |
| GET | `/api/doctors/me` | DOCTOR | Get own profile |
| PATCH | `/api/doctors/me` | DOCTOR | Update phone/specialization |

### Parents — `/api/parents`

| Method | Path | Roles | Description |
|---|---|---|---|
| GET | `/api/parents/me` | PARENT | Get own profile |
| PATCH | `/api/parents/me` | PARENT | Update phone/address |

### Children — `/api/children`

| Method | Path | Roles | Description |
|---|---|---|---|
| POST | `/api/children` | PARENT | Add a child |
| GET | `/api/children` | PARENT, DOCTOR | List own children / assigned patients |
| GET | `/api/children/{id}` | PARENT, DOCTOR | Get child by ID |
| PUT | `/api/children/{id}` | PARENT | Update child details |
| DELETE | `/api/children/{id}` | PARENT | Remove a child |
| POST | `/api/children/{childId}/doctors/{doctorId}` | PARENT | Assign a doctor |
| DELETE | `/api/children/{childId}/doctors/{doctorId}` | PARENT | Remove a doctor |

### Appointments — `/api/appointments`

| Method | Path | Roles | Description |
|---|---|---|---|
| POST | `/api/appointments` | PARENT, DOCTOR | Create appointment |
| GET | `/api/appointments` | PARENT, DOCTOR | List own appointments |
| GET | `/api/appointments/child/{childId}` | PARENT, DOCTOR | Appointments for a child |
| PATCH | `/api/appointments/{id}/status` | PARENT, DOCTOR | Update status (SCHEDULED / COMPLETED / CANCELLED) |

### Medical Records — `/api/medical-records`

| Method | Path | Roles | Description |
|---|---|---|---|
| POST | `/api/medical-records` | **DOCTOR only** | Create a record |
| GET | `/api/medical-records/child/{childId}` | PARENT, DOCTOR | Records for a child |
| GET | `/api/medical-records/{id}` | PARENT, DOCTOR | Get record by ID |
| PUT | `/api/medical-records/{id}` | **DOCTOR only** | Update a record |

### Vaccinations — `/api/vaccinations`

| Method | Path | Roles | Description |
|---|---|---|---|
| POST | `/api/vaccinations` | **DOCTOR only** | Log a vaccination |
| GET | `/api/vaccinations/child/{childId}` | PARENT, DOCTOR | Vaccinations for a child |
| GET | `/api/vaccinations/{id}` | PARENT, DOCTOR | Get vaccination by ID |
| PUT | `/api/vaccinations/{id}` | **DOCTOR only** | Update a vaccination |

### Allergies — `/api/allergies`

| Method | Path | Roles | Description |
|---|---|---|---|
| POST | `/api/allergies` | PARENT, DOCTOR | Record an allergy |
| GET | `/api/allergies/child/{childId}` | PARENT, DOCTOR | Allergies for a child |
| GET | `/api/allergies/{id}` | PARENT, DOCTOR | Get allergy by ID |
| PUT | `/api/allergies/{id}` | PARENT, DOCTOR | Update an allergy |
| DELETE | `/api/allergies/{id}` | PARENT, DOCTOR | Delete an allergy |

---

## Sensitive Data Encryption

All personally identifiable and medically sensitive fields are encrypted in the database using **AES-256-GCM** with a random 96-bit IV per value. The ciphertext (IV + encrypted bytes) is base64-encoded and stored in `TEXT` columns.

Encryption is transparent to the application via the JPA `AttributeConverter` (`EncryptionConverter`), which wraps `EncryptionUtil`.

### Encrypted fields

| Entity | Encrypted fields |
|---|---|
| Doctor | `firstName`, `lastName`, `email`, `phoneNumber`, `licenseNumber` |
| Parent | `firstName`, `lastName`, `email`, `phoneNumber`, `address` |
| Child | `firstName`, `lastName`, `dateOfBirth`, `bloodType` |
| Appointment | `reason`, `notes` |
| MedicalRecord | `diagnosis`, `notes`, `prescription` |
| Vaccination | `batchNumber`, `notes` |
| Allergy | `allergen`, `reaction`, `notes` |

---

## Configuration

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/child_healthcare_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=YOUR_DB_PASSWORD

app.jwt.secret=YOUR_RANDOM_32_CHAR_SECRET
app.jwt.expiration-ms=86400000

# Generate with: openssl rand -base64 32
app.encryption.key=YOUR_BASE64_AES_256_KEY
```

> **Production note:** Use environment variables for all secrets — never commit real keys to version control.

---

## Running the Application

```bash
# Build
mvn clean package

# Run
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`.

---

## Example Flow

1. Register a parent: `POST /api/auth/register/parent`
2. Register a doctor: `POST /api/auth/register/doctor`
3. Login as parent, get JWT: `POST /api/auth/login`
4. Add a child: `POST /api/children` *(with parent JWT)*
5. Assign the doctor to the child: `POST /api/children/{childId}/doctors/{doctorId}`
6. Login as doctor, create a medical record: `POST /api/medical-records`
7. Parent reads the record: `GET /api/medical-records/child/{childId}`
