package com.healthcare.childrecords.dto.response;

public record AuthResponse(String token, String role, Long profileId) {}
