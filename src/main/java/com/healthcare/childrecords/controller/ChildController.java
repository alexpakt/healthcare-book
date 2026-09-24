package com.healthcare.childrecords.controller;

import com.healthcare.childrecords.dto.request.ChildRequest;
import com.healthcare.childrecords.dto.response.ChildResponse;
import com.healthcare.childrecords.service.ChildService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/children")
@RequiredArgsConstructor
@Tag(name = "Children", description = "Child health record management endpoints")
@SecurityRequirement(name = "bearer")
public class ChildController {

    private final ChildService childService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new child record", description = "Create a new child profile linked to the authenticated parent")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Child record created successfully",
            content = @Content(schema = @Schema(implementation = ChildResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ChildResponse create(@AuthenticationPrincipal UserDetails userDetails,
                                @Valid @RequestBody ChildRequest request) {
        return childService.create(userDetails.getUsername(), request);
    }

    @GetMapping
    @Operation(summary = "List children", description = "Retrieve all children associated with the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of children retrieved",
            content = @Content(schema = @Schema(implementation = ChildResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public List<ChildResponse> getMyChildren(@AuthenticationPrincipal UserDetails userDetails) {
        return childService.getMyChildren(userDetails.getUsername());
    }

    @GetMapping("/search")
    @Operation(summary = "Search child by AMKA", description = "Search for a child using their AMKA (Greek Social Security Number)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Child found",
            content = @Content(schema = @Schema(implementation = ChildResponse.class))),
        @ApiResponse(responseCode = "404", description = "Child not found")
    })
    public ChildResponse searchByAmka(@Parameter(description = "AMKA (11 digits)") @RequestParam String amka) {
        return childService.searchByAmka(amka);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get child by ID", description = "Retrieve a specific child record if authorized")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Child record retrieved",
            content = @Content(schema = @Schema(implementation = ChildResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Child not found")
    })
    public ChildResponse getById(@AuthenticationPrincipal UserDetails userDetails,
                                 @Parameter(description = "Child ID") @PathVariable Long id) {
        return childService.getById(userDetails.getUsername(), id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update child record", description = "Update an existing child's profile information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Child record updated",
            content = @Content(schema = @Schema(implementation = ChildResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Child not found")
    })
    public ChildResponse update(@AuthenticationPrincipal UserDetails userDetails,
                                @Parameter(description = "Child ID") @PathVariable Long id,
                                @Valid @RequestBody ChildRequest request) {
        return childService.update(userDetails.getUsername(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete child record", description = "Delete a child record (only parents can delete)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Child record deleted"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Child not found")
    })
    public void delete(@AuthenticationPrincipal UserDetails userDetails,
                       @Parameter(description = "Child ID") @PathVariable Long id) {
        childService.delete(userDetails.getUsername(), id);
    }

    @PostMapping("/{childId}/doctors/{doctorId}")
    @Operation(summary = "Assign doctor to child", description = "Link a pediatrician to a child record")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Doctor assigned successfully",
            content = @Content(schema = @Schema(implementation = ChildResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Child or doctor not found")
    })
    public ChildResponse assignDoctor(@AuthenticationPrincipal UserDetails userDetails,
                                      @Parameter(description = "Child ID") @PathVariable Long childId,
                                      @Parameter(description = "Doctor ID") @PathVariable Long doctorId) {
        return childService.assignDoctor(userDetails.getUsername(), childId, doctorId);
    }

    @DeleteMapping("/{childId}/doctors/{doctorId}")
    @Operation(summary = "Remove doctor from child", description = "Unlink a pediatrician from a child record")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Doctor removed successfully",
            content = @Content(schema = @Schema(implementation = ChildResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Child or doctor not found")
    })
    public ChildResponse removeDoctor(@AuthenticationPrincipal UserDetails userDetails,
                                      @Parameter(description = "Child ID") @PathVariable Long childId,
                                      @Parameter(description = "Doctor ID") @PathVariable Long doctorId) {
        return childService.removeDoctor(userDetails.getUsername(), childId, doctorId);
    }
}
