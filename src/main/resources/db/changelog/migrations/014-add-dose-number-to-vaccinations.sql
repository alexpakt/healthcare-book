-- liquibase formatted sql

-- changeset alexandra:add-dose-number-column
-- comment: Add dose_number to track which dose of a vaccine was administered
ALTER TABLE vaccinations ADD COLUMN dose_number INT;
CREATE INDEX idx_vaccination_vaccine_dose ON vaccinations(vaccine_name, dose_number);
