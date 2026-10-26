package com.healthcare.childrecords.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

/** Partial update: a field that is null is left unchanged. */
public record UpdateParentProfileRequest(
        @Pattern(regexp = ".*\\S.*", message = "First name must not be blank") String firstName,
        @Pattern(regexp = ".*\\S.*", message = "Last name must not be blank") String lastName,
        @Email String email,
        String phoneNumber,
        String address
) {}
