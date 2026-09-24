-- liquibase formatted sql

-- changeset alexandra:create-vaccines-table
-- comment: Create vaccines table to normalize vaccine master data
CREATE TABLE vaccines (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vaccine_name VARCHAR(100) NOT NULL UNIQUE,
    vaccine_name_gr VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- changeset alexandra:populate-vaccines-from-schedules
-- comment: Extract unique vaccines from vaccination_schedules into vaccines table
INSERT INTO vaccines (vaccine_name, vaccine_name_gr)
SELECT DISTINCT vaccine_name, vaccine_name_gr
FROM vaccination_schedules
ORDER BY vaccine_name;

-- changeset alexandra:add-vaccine-id-to-schedules
-- comment: Add vaccine_id column and foreign key constraint to vaccination_schedules
ALTER TABLE vaccination_schedules ADD COLUMN vaccine_id BIGINT AFTER id;

-- Populate vaccine_id from the vaccines table
UPDATE vaccination_schedules vs
SET vaccine_id = (
    SELECT v.id FROM vaccines v
    WHERE v.vaccine_name = vs.vaccine_name
);

-- Add foreign key constraint
ALTER TABLE vaccination_schedules
ADD CONSTRAINT fk_vaccination_schedules_vaccine
FOREIGN KEY (vaccine_id) REFERENCES vaccines(id);

-- changeset alexandra:remove-duplicate-vaccine-columns
-- comment: Drop original vaccine_name and vaccine_name_gr from vaccination_schedules
ALTER TABLE vaccination_schedules DROP COLUMN vaccine_name;
ALTER TABLE vaccination_schedules DROP COLUMN vaccine_name_gr;

-- changeset alexandra:create-new-indexes
-- comment: Create new indexes for optimized queries on normalized schema
CREATE INDEX idx_vaccination_schedule_vaccine_id ON vaccination_schedules(vaccine_id);
CREATE INDEX idx_vaccination_schedule_age ON vaccination_schedules(recommended_age_months);
CREATE INDEX idx_vaccination_schedule_vaccine_id_dose ON vaccination_schedules(vaccine_id, dose_number);
