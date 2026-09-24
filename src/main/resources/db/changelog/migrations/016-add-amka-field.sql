-- liquibase formatted sql

-- changeset alexandra:add-amka-to-children
-- comment: Add AMKA as mandatory unique field to children table
ALTER TABLE children ADD COLUMN amka VARCHAR(255) NOT NULL UNIQUE;
CREATE INDEX idx_children_amka ON children(amka);

-- changeset alexandra:add-amka-to-doctors
-- comment: Add AMKA as mandatory unique field to doctors table
ALTER TABLE doctors ADD COLUMN amka VARCHAR(255) NOT NULL UNIQUE;
CREATE INDEX idx_doctors_amka ON doctors(amka);
