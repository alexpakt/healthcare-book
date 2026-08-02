package com.healthcare.childrecords.controller;

import com.healthcare.childrecords.dto.request.GrowthMeasurementRequest;
import com.healthcare.childrecords.dto.response.GrowthMeasurementResponse;
import com.healthcare.childrecords.service.GrowthMeasurementService;
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
public class GrowthMeasurementController {

    private final GrowthMeasurementService growthMeasurementService;

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
}
