-- liquibase formatted sql

-- changeset alexandra:create-birth-records-table
-- comment: Create birth records table
CREATE TABLE birth_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    birth_weight_grams INT,
    birth_length_cm DOUBLE,
    birth_head_circumference_cm DOUBLE,
    gestational_age_weeks INT,
    delivery_type ENUM('VAGINAL','CESAREAN','VAGINAL_ASSISTED'),
    apgar_score_1_min INT,
    apgar_score_5_min INT,
    birth_hospital TEXT,
    nicu_admission BIT(1),
    nicu_duration_days INT,
    notes TEXT,
    child_id BIGINT NOT NULL UNIQUE,
    CONSTRAINT fk_birth_records_child FOREIGN KEY (child_id) REFERENCES children(id)
);
