package com.healthcare.childrecords.dto.response;

/** Service-level login outcome: the JWT goes into a cookie, the rest into the response body. */
public record AuthResult(String token, String role, Long profileId) {

    public AuthResponse toResponse() {
        return new AuthResponse(role, profileId);
    }
}
