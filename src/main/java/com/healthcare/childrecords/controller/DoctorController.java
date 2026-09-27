package com.healthcare.childrecords.controller;

import com.healthcare.childrecords.dto.response.DoctorResponse;
import com.healthcare.childrecords.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
@Tag(name = "Doctors", description = "Doctor profile and search endpoints")
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping
    @Operation(summary = "List all doctors", description = "Retrieve all registered doctors")
    public List<DoctorResponse> getAll() {
        return doctorService.getAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get doctor by ID", description = "Retrieve a specific doctor's profile")
    public DoctorResponse getById(@Parameter(description = "Doctor ID") @PathVariable Long id) {
        return doctorService.getById(id);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current doctor profile", description = "Retrieve the authenticated doctor's profile")
    @SecurityRequirement(name = "bearer")
    public DoctorResponse getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return doctorService.getMyProfile(userDetails.getUsername());
    }

    @PatchMapping("/me")
    @Operation(summary = "Update current doctor profile", description = "Update authenticated doctor's phone number, specialization, and address")
    @SecurityRequirement(name = "bearer")
    public DoctorResponse updateMyProfile(@AuthenticationPrincipal UserDetails userDetails,
                                          @RequestBody Map<String, String> updates) {
        return doctorService.updateMyProfile(
                userDetails.getUsername(),
                updates.get("phoneNumber"),
                updates.get("specialization"),
                updates.get("address"));
    }

    @GetMapping("/search/amka")
    @Operation(summary = "Search doctor by AMKA", description = "Search for a doctor using their AMKA (Greek Social Security Number)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Doctor found",
            content = @Content(schema = @Schema(implementation = DoctorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Doctor not found")
    })
    public DoctorResponse searchByAmka(@Parameter(description = "AMKA (10 digits)") @RequestParam String amka) {
        return doctorService.searchByAmka(amka);
    }

    @GetMapping("/search/name")
    @Operation(summary = "Search doctors by name", description = "Search for doctors using first or last name (case-insensitive)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Doctors found",
            content = @Content(schema = @Schema(implementation = DoctorResponse.class))),
        @ApiResponse(responseCode = "404", description = "No doctors found")
    })
    public List<DoctorResponse> searchByName(@Parameter(description = "First or last name (partial match allowed)") @RequestParam String name) {
        return doctorService.searchByName(name);
    }
}
