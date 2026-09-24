-- liquibase formatted sql

-- changeset alexandra:add-parent-vaccination-id-column
-- comment: Add parent_vaccination_id to track combination vaccine components
ALTER TABLE vaccinations ADD COLUMN parent_vaccination_id BIGINT;
ALTER TABLE vaccinations ADD CONSTRAINT fk_vaccination_parent FOREIGN KEY (parent_vaccination_id) REFERENCES vaccinations(id) ON DELETE CASCADE;
CREATE INDEX idx_vaccination_parent_id ON vaccinations(parent_vaccination_id);
