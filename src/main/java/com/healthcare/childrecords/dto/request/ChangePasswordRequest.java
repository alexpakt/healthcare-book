package com.healthcare.childrecords.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank String currentPassword,
        @NotBlank @Size(min = 8, message = "Ο κωδικός πρόσβασης πρέπει να έχει τουλάχιστον 8 χαρακτήρες") String newPassword
) {}
