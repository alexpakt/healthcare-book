-- liquibase formatted sql

-- changeset alexandra:create-growth-measurements-table
-- comment: Create growth measurements table
CREATE TABLE growth_measurements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    measurement_date DATE NOT NULL,
    age_in_months INT NOT NULL,
    height_cm DOUBLE,
    weight_kg DOUBLE,
    head_circumference_cm DOUBLE,
    notes TEXT,
    child_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    CONSTRAINT fk_growth_measurements_child FOREIGN KEY (child_id) REFERENCES children(id),
    CONSTRAINT fk_growth_measurements_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(id)
);
