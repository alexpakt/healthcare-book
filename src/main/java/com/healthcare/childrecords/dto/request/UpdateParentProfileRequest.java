package com.healthcare.childrecords.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

/** Partial update: a field that is null is left unchanged. */
public record UpdateParentProfileRequest(
        @Pattern(regexp = ".*\\S.*", message = "Το όνομα δεν μπορεί να είναι κενό") String firstName,
        @Pattern(regexp = ".*\\S.*", message = "Το επώνυμο δεν μπορεί να είναι κενό") String lastName,
        @Email String email,
        String phoneNumber,
        String address
) {}
