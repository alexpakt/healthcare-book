package com.healthcare.childrecords.dto.response;

import com.healthcare.childrecords.entity.Gender;

public record ChildResponse(
        Long id,
        String firstName,
        String lastName,
        String dateOfBirth,
        Gender gender,
        String bloodType,
        Long parentId
) {}
