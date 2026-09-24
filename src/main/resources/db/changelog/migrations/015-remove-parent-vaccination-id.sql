-- liquibase formatted sql

-- changeset alexandra:remove-parent-vaccination-id-column
-- comment: Remove parent_vaccination_id column as we no longer use parent records for combinations
ALTER TABLE vaccinations DROP FOREIGN KEY fk_vaccination_parent;
ALTER TABLE vaccinations DROP INDEX idx_vaccination_parent_id;
ALTER TABLE vaccinations DROP COLUMN parent_vaccination_id;
