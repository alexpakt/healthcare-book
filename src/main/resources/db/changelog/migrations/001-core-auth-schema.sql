-- liquibase formatted sql

-- changeset alexandra:create-users-table
-- comment: Create users table for authentication
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('ROLE_DOCTOR','ROLE_PARENT') NOT NULL,
    enabled BIT(1) NOT NULL
);

-- changeset alexandra:create-doctors-table
-- comment: Create doctors table
CREATE TABLE doctors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    email TEXT NOT NULL,
    phone_number TEXT,
    specialization VARCHAR(255),
    license_number TEXT NOT NULL,
    user_id BIGINT NOT NULL UNIQUE,
    CONSTRAINT fk_doctors_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- changeset alexandra:create-parents-table
-- comment: Create parents table
CREATE TABLE parents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    email TEXT NOT NULL,
    phone_number TEXT,
    address TEXT,
    user_id BIGINT NOT NULL UNIQUE,
    CONSTRAINT fk_parents_user FOREIGN KEY (user_id) REFERENCES users(id)
);
