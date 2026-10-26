package com.healthcare.childrecords.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

/** Partial update: a field that is null is left unchanged. The AMKA identifies the doctor and cannot be changed. */
public record UpdateDoctorProfileRequest(
        @Pattern(regexp = ".*\\S.*", message = "First name must not be blank") String firstName,
        @Pattern(regexp = ".*\\S.*", message = "Last name must not be blank") String lastName,
        @Email String email,
        String phoneNumber,
        @Pattern(regexp = ".*\\S.*", message = "Specialization must not be blank") String specialization,
        @Pattern(regexp = ".*\\S.*", message = "License number must not be blank") String licenseNumber,
        String address
) {}
