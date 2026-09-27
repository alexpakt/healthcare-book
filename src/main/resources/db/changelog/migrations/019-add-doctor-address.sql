-- liquibase formatted sql

-- changeset alexandra:add-doctor-address-column
-- comment: Add address column to doctors table
ALTER TABLE doctors ADD COLUMN address VARCHAR(500);
