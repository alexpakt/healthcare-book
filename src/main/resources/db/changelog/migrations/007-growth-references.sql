-- liquibase formatted sql

-- changeset alexandra:create-growth-references-table
-- comment: Create growth_references table with WHO child growth standards
CREATE TABLE growth_references (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    age_in_months INT NOT NULL,
    measurement_type ENUM('HEIGHT','WEIGHT','HEAD_CIRCUMFERENCE') NOT NULL,
    gender ENUM('MALE','FEMALE','OTHER') NOT NULL,
    percentile INT NOT NULL,
    value DOUBLE NOT NULL,
    source VARCHAR(50) NOT NULL,
    CONSTRAINT uk_growth_reference_composite UNIQUE (age_in_months, measurement_type, gender, percentile, source)
);

CREATE INDEX idx_growth_ref_age_type_gender ON growth_references(age_in_months, measurement_type, gender);
