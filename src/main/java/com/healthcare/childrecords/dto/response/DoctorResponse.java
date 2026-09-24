package com.healthcare.childrecords.dto.response;

public record DoctorResponse(
        Long id,
        String firstName,
        String lastName,
        String amka,
        String email,
        String phoneNumber,
        String specialization,
        String licenseNumber
) {}
