package com.healthcare.childrecords.controller;

import com.healthcare.childrecords.dto.request.ChangePasswordRequest;
import com.healthcare.childrecords.dto.request.LoginRequest;
import com.healthcare.childrecords.dto.request.RegisterDoctorRequest;
import com.healthcare.childrecords.dto.request.RegisterParentRequest;
import com.healthcare.childrecords.dto.response.AuthResponse;
import com.healthcare.childrecords.dto.response.AuthResult;
import com.healthcare.childrecords.dto.response.CurrentUserResponse;
import com.healthcare.childrecords.security.AuthCookieService;
import com.healthcare.childrecords.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User registration and login endpoints")
public class AuthController {

    private final AuthService authService;
    private final AuthCookieService cookieService;

    @PostMapping("/register/doctor")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new pediatrician", description = "Create a new doctor account with ROLE_DOCTOR")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Doctor registered successfully",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input - missing required fields"),
        @ApiResponse(responseCode = "409", description = "Username already exists")
    })
    public ResponseEntity<AuthResponse> registerDoctor(@Valid @RequestBody RegisterDoctorRequest request) {
        return withAuthCookie(HttpStatus.CREATED, authService.registerDoctor(request));
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
    public ResponseEntity<AuthResponse> registerParent(@Valid @RequestBody RegisterParentRequest request) {
        return withAuthCookie(HttpStatus.CREATED, authService.registerParent(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Login with username and password to receive an HttpOnly auth cookie")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful, auth cookie set",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "400", description = "Missing username or password"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return withAuthCookie(HttpStatus.OK, authService.login(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "Log out", description = "Clears the authentication cookie")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookieService.clear())
                .build();
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Returns the user identified by the auth cookie (or a bearer token)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Current user",
            content = @Content(schema = @Schema(implementation = CurrentUserResponse.class))),
        @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    public CurrentUserResponse me(@AuthenticationPrincipal UserDetails userDetails) {
        return authService.getCurrentUser(userDetails.getUsername());
    }

    @PostMapping("/change-password")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Change user password", description = "Change password for authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password changed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input or current password incorrect"),
        @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    public void changePassword(@AuthenticationPrincipal UserDetails userDetails,
                               @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(userDetails.getUsername(), request);
    }

    private ResponseEntity<AuthResponse> withAuthCookie(HttpStatus status, AuthResult result) {
        return ResponseEntity.status(status)
                .header(HttpHeaders.SET_COOKIE, cookieService.create(result.token()))
                .body(result.toResponse());
    }
}
