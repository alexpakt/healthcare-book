-- liquibase formatted sql

-- changeset alexandra:create-doctor-child-requests-table
-- comment: Create table for doctor-child connection requests
CREATE TABLE doctor_child_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id BIGINT NOT NULL,
    child_id BIGINT NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP,
    notes VARCHAR(255),
    CONSTRAINT fk_doctor_child_requests_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(id),
    CONSTRAINT fk_doctor_child_requests_child FOREIGN KEY (child_id) REFERENCES children(id),
    CONSTRAINT unique_doctor_child_request UNIQUE (doctor_id, child_id, status)
);

CREATE INDEX idx_doctor_child_requests_doctor_id ON doctor_child_requests(doctor_id);
CREATE INDEX idx_doctor_child_requests_child_id ON doctor_child_requests(child_id);
CREATE INDEX idx_doctor_child_requests_status ON doctor_child_requests(status);
