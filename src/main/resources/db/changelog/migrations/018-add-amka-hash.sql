-- liquibase formatted sql

-- changeset alexandra:add-amka-hash-to-children
-- comment: Add amka_hash column for searchable AMKA lookup
ALTER TABLE children ADD COLUMN amka_hash VARCHAR(64);

-- changeset alexandra:add-amka-hash-to-doctors
-- comment: Add amka_hash column for searchable AMKA lookup
ALTER TABLE doctors ADD COLUMN amka_hash VARCHAR(64);

-- changeset alexandra:create-index-children-amka-hash
-- comment: Create index on amka_hash for faster searches
CREATE INDEX idx_children_amka_hash ON children(amka_hash);

-- changeset alexandra:create-index-doctors-amka-hash
-- comment: Create index on amka_hash for faster searches
CREATE INDEX idx_doctors_amka_hash ON doctors(amka_hash);
