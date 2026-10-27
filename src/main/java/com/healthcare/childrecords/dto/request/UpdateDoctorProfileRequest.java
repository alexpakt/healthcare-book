package com.healthcare.childrecords.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

/** Partial update: a field that is null is left unchanged. The AMKA identifies the doctor and cannot be changed. */
public record UpdateDoctorProfileRequest(
        @Pattern(regexp = ".*\\S.*", message = "Το όνομα δεν μπορεί να είναι κενό") String firstName,
        @Pattern(regexp = ".*\\S.*", message = "Το επώνυμο δεν μπορεί να είναι κενό") String lastName,
        @Email String email,
        String phoneNumber,
        @Pattern(regexp = ".*\\S.*", message = "Η ειδικότητα δεν μπορεί να είναι κενή") String specialization,
        @Pattern(regexp = ".*\\S.*", message = "Ο αριθμός άδειας δεν μπορεί να είναι κενός") String licenseNumber,
        String address
) {}
