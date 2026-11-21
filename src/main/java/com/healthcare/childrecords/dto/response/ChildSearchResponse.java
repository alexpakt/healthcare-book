package com.healthcare.childrecords.dto.response;

public record ChildSearchResponse(
        Long id,
        String firstName,
        String lastName,
        String amka
) {}
