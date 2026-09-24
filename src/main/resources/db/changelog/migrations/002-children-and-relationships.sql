-- liquibase formatted sql

-- changeset alexandra:create-children-table
-- comment: Create children table
CREATE TABLE children (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    date_of_birth TEXT NOT NULL,
    gender ENUM('MALE','FEMALE','OTHER') NOT NULL,
    blood_type TEXT,
    parent_id BIGINT NOT NULL,
    CONSTRAINT fk_children_parent FOREIGN KEY (parent_id) REFERENCES parents(id)
);

CREATE INDEX idx_children_parent_id ON children(parent_id);

-- changeset alexandra:create-child-doctor-join-table
-- comment: Create join table for child-doctor relationships
CREATE TABLE child_doctor (
    child_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    CONSTRAINT fk_child_doctor_child FOREIGN KEY (child_id) REFERENCES children(id),
    CONSTRAINT fk_child_doctor_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(id)
);

CREATE INDEX idx_child_doctor_doctor_id ON child_doctor(doctor_id);
