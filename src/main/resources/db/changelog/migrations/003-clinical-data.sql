-- liquibase formatted sql

-- changeset alexandra:create-appointments-table
-- comment: Create appointments table
CREATE TABLE appointments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    appointment_date DATETIME NOT NULL,
    reason TEXT,
    notes TEXT,
    status ENUM('SCHEDULED','COMPLETED','CANCELLED') NOT NULL,
    child_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    CONSTRAINT fk_appointments_child FOREIGN KEY (child_id) REFERENCES children(id),
    CONSTRAINT fk_appointments_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(id)
);

-- changeset alexandra:create-medical-records-table
-- comment: Create medical records table
CREATE TABLE medical_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_date DATE NOT NULL,
    diagnosis TEXT NOT NULL,
    notes TEXT,
    prescription TEXT,
    child_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    appointment_id BIGINT UNIQUE,
    CONSTRAINT fk_medical_records_child FOREIGN KEY (child_id) REFERENCES children(id),
    CONSTRAINT fk_medical_records_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(id),
    CONSTRAINT fk_medical_records_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id)
);

-- changeset alexandra:create-vaccinations-table
-- comment: Create vaccinations table
CREATE TABLE vaccinations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vaccine_name VARCHAR(255) NOT NULL,
    administered_date DATE NOT NULL,
    next_dose_date DATE,
    batch_number TEXT,
    notes TEXT,
    child_id BIGINT NOT NULL,
    administered_by_doctor_id BIGINT,
    CONSTRAINT fk_vaccinations_child FOREIGN KEY (child_id) REFERENCES children(id),
    CONSTRAINT fk_vaccinations_doctor FOREIGN KEY (administered_by_doctor_id) REFERENCES doctors(id)
);

-- changeset alexandra:create-allergies-table
-- comment: Create allergies table
CREATE TABLE allergies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    allergen TEXT NOT NULL,
    severity ENUM('MILD','MODERATE','SEVERE') NOT NULL,
    reaction TEXT,
    diagnosed_date DATE,
    notes TEXT,
    child_id BIGINT NOT NULL,
    CONSTRAINT fk_allergies_child FOREIGN KEY (child_id) REFERENCES children(id)
);
