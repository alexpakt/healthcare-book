package com.healthcare.childrecords.dto.request;

import com.healthcare.childrecords.entity.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ChildRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank @Pattern(regexp = "\\d{11}", message = "O ΑΜΚΑ πρέπει να αποτελείται από 11 ψηφία") String amka,
        @NotBlank @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Η ημερομηνία πρέπει να έχει μορφή ΕΕΕΕ-ΜΜ-ΗΗ") String dateOfBirth,
        @NotNull Gender gender,
        String bloodType
) {}
