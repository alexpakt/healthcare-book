package com.healthcare.childrecords.dto.response;

public record ParentResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String address
) {}
