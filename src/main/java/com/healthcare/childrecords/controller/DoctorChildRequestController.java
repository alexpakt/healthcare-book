package com.healthcare.childrecords.controller;

import com.healthcare.childrecords.dto.request.DoctorChildConnectionRequest;
import com.healthcare.childrecords.dto.response.DoctorChildRequestResponse;
import com.healthcare.childrecords.service.DoctorChildRequestService;
import com.healthcare.childrecords.service.ParentService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctor-child-requests")
@RequiredArgsConstructor
@Tag(name = "Doctor-Child Requests", description = "Manage doctor-child connection requests")
@SecurityRequirement(name = "bearer")
public class DoctorChildRequestController {

    private final DoctorChildRequestService requestService;
    private final ParentService parentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Send connection request", description = "Doctor requests to connect with a child patient")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Request created successfully",
                    content = @Content(schema = @Schema(implementation = DoctorChildRequestResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Child not found")
    })
    @PreAuthorize("hasRole('DOCTOR')")
    public DoctorChildRequestResponse sendRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody DoctorChildConnectionRequest request) {
        return requestService.sendConnectionRequest(userDetails.getUsername(), request);
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending requests for parent", description = "Retrieve all pending connection requests for the authenticated parent's children")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pending requests retrieved",
                    content = @Content(schema = @Schema(implementation = DoctorChildRequestResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PreAuthorize("hasRole('PARENT')")
    public List<DoctorChildRequestResponse> getPendingRequests(@AuthenticationPrincipal UserDetails userDetails) {
        Long parentId = parentService.findByUsername(userDetails.getUsername()).getId();
        return requestService.getPendingRequestsForParent(parentId);
    }

    @GetMapping("/pending/sent")
    @Operation(summary = "Get sent pending requests", description = "Retrieve all pending connection requests sent by the authenticated doctor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sent pending requests retrieved",
                    content = @Content(schema = @Schema(implementation = DoctorChildRequestResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PreAuthorize("hasRole('DOCTOR')")
    public List<DoctorChildRequestResponse> getSentPendingRequests(@AuthenticationPrincipal UserDetails userDetails) {
        return requestService.getSentPendingRequests(userDetails.getUsername());
    }

    @PostMapping("/{requestId}/approve")
    @Operation(summary = "Approve connection request", description = "Parent approves a doctor's connection request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request approved successfully",
                    content = @Content(schema = @Schema(implementation = DoctorChildRequestResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Request not found")
    })
    @PreAuthorize("hasRole('PARENT')")
    public DoctorChildRequestResponse approveRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Request ID") @PathVariable Long requestId) {
        return requestService.approveRequest(userDetails.getUsername(), requestId);
    }

    @PostMapping("/{requestId}/reject")
    @Operation(summary = "Reject connection request", description = "Parent rejects a doctor's connection request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request rejected successfully",
                    content = @Content(schema = @Schema(implementation = DoctorChildRequestResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Request not found")
    })
    @PreAuthorize("hasRole('PARENT')")
    public DoctorChildRequestResponse rejectRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Request ID") @PathVariable Long requestId,
            @RequestParam(required = false) String reason) {
        return requestService.rejectRequest(userDetails.getUsername(), requestId, reason);
    }
}
