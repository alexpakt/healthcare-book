package com.healthcare.childrecords.controller;

import com.healthcare.childrecords.dto.request.LoginRequest;
import com.healthcare.childrecords.dto.request.RegisterDoctorRequest;
import com.healthcare.childrecords.dto.request.RegisterParentRequest;
import com.healthcare.childrecords.dto.response.AuthResponse;
import com.healthcare.childrecords.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User registration and login endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register/doctor")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new pediatrician", description = "Create a new doctor account with ROLE_DOCTOR")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Doctor registered successfully",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input - missing required fields"),
        @ApiResponse(responseCode = "409", description = "Username already exists")
    })
    public AuthResponse registerDoctor(@Valid @RequestBody RegisterDoctorRequest request) {
        return authService.registerDoctor(request);
    }

    @PostMapping("/register/parent")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new parent/guardian", description = "Create a new parent account with ROLE_PARENT")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Parent registered successfully",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input - missing required fields"),
        @ApiResponse(responseCode = "409", description = "Username already exists")
    })
    public AuthResponse registerParent(@Valid @RequestBody RegisterParentRequest request) {
        return authService.registerParent(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Login with username and password to receive JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful, JWT token returned",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "400", description = "Missing username or password"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
