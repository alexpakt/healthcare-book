package com.healthcare.childrecords.controller;

import com.healthcare.childrecords.dto.request.GrowthAlertAcknowledgeRequest;
import com.healthcare.childrecords.dto.request.GrowthMeasurementRequest;
import com.healthcare.childrecords.dto.response.GrowthAlertResponse;
import com.healthcare.childrecords.dto.response.GrowthMeasurementResponse;
import com.healthcare.childrecords.entity.GrowthReference;
import com.healthcare.childrecords.entity.MeasurementType;
import com.healthcare.childrecords.service.GrowthMeasurementService;
import com.healthcare.childrecords.service.GrowthTrajectoryService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/growth-measurements")
@RequiredArgsConstructor
@Tag(name = "Growth Measurements", description = "Child growth measurement management endpoint")
public class GrowthMeasurementController {

    private final GrowthMeasurementService growthMeasurementService;
    private final GrowthTrajectoryService growthTrajectoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('DOCTOR')")
    public GrowthMeasurementResponse create(@AuthenticationPrincipal UserDetails userDetails,
                                            @Valid @RequestBody GrowthMeasurementRequest request) {
        return growthMeasurementService.create(userDetails.getUsername(), request);
    }

    @GetMapping("/child/{childId}")
    public List<GrowthMeasurementResponse> getForChild(@AuthenticationPrincipal UserDetails userDetails,
                                                       @PathVariable Long childId) {
        return growthMeasurementService.getForChild(userDetails.getUsername(), childId);
    }

    @GetMapping("/{id}")
    public GrowthMeasurementResponse getById(@AuthenticationPrincipal UserDetails userDetails,
                                             @PathVariable Long id) {
        return growthMeasurementService.getById(userDetails.getUsername(), id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public GrowthMeasurementResponse update(@AuthenticationPrincipal UserDetails userDetails,
                                            @PathVariable Long id,
                                            @Valid @RequestBody GrowthMeasurementRequest request) {
        return growthMeasurementService.update(userDetails.getUsername(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('DOCTOR')")
    public void delete(@AuthenticationPrincipal UserDetails userDetails,
                       @PathVariable Long id) {
        growthMeasurementService.delete(userDetails.getUsername(), id);
    }

    @GetMapping("/child/{childId}/alerts")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "List growth alerts for clinical review (doctors only). Alerts are monitoring aids, not diagnoses.")
    public List<GrowthAlertResponse> getAlerts(@AuthenticationPrincipal UserDetails userDetails,
                                               @PathVariable Long childId) {
        return growthTrajectoryService.getAlerts(userDetails.getUsername(), childId);
    }

    @PostMapping("/child/{childId}/alerts/{alertKey}/acknowledge")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Acknowledge a growth alert after clinical review.")
    public GrowthAlertResponse acknowledgeAlert(@AuthenticationPrincipal UserDetails userDetails,
                                                @PathVariable Long childId,
                                                @PathVariable String alertKey,
                                                @Valid @RequestBody(required = false) GrowthAlertAcknowledgeRequest request) {
        String note = request == null ? null : request.note();
        return growthTrajectoryService.acknowledge(userDetails.getUsername(), childId, alertKey, note);
    }

    @GetMapping("/references/child/{childId}/type/{measurementType}")
    public List<GrowthReference> getReferenceCurves(@PathVariable Long childId,
                                                    @PathVariable String measurementType) {
        return growthMeasurementService.getReferenceCurves(childId, MeasurementType.valueOf(measurementType));
    }
}
