package com.healthcare.childrecords.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterDoctorRequest(
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank @Pattern(regexp = "\\d{11}", message = "AMKA must be 11 digits") String amka,
        @NotBlank @Email String email,
        String phoneNumber,
        @NotBlank String specialization,
        @NotBlank String licenseNumber,
        String address
) {}
