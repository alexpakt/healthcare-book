-- liquibase formatted sql

-- changeset alexandra:add-appointment-status-enum-values
-- comment: Alter the AppointmentStatus column to add the new values (REQUESTED, DECLINED)
ALTER TABLE appointments
MODIFY COLUMN status ENUM('REQUESTED','SCHEDULED','DECLINED','COMPLETED','CANCELLED') NOT NULL;
