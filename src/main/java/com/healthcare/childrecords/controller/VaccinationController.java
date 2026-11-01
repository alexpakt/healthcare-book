package com.healthcare.childrecords.controller;

import com.healthcare.childrecords.dto.request.VaccinationRequest;
import com.healthcare.childrecords.dto.response.VaccinationResponse;
import com.healthcare.childrecords.service.VaccinationService;
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
@RequestMapping("/api/vaccinations")
@RequiredArgsConstructor
@Tag(name = "Vaccinations", description = "Child vaccination management endpoint")
public class VaccinationController {

    private final VaccinationService vaccinationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('DOCTOR')")
    public VaccinationResponse create(@AuthenticationPrincipal UserDetails userDetails,
                                      @Valid @RequestBody VaccinationRequest request) {
        return vaccinationService.create(userDetails.getUsername(), request);
    }

    @GetMapping("/child/{childId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PARENT')")
    public List<VaccinationResponse> getForChild(@AuthenticationPrincipal UserDetails userDetails,
                                                 @PathVariable Long childId) {
        return vaccinationService.getForChild(userDetails.getUsername(), childId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PARENT')")
    public VaccinationResponse getById(@AuthenticationPrincipal UserDetails userDetails,
                                       @PathVariable Long id) {
        return vaccinationService.getById(userDetails.getUsername(), id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public VaccinationResponse update(@AuthenticationPrincipal UserDetails userDetails,
                                      @PathVariable Long id,
                                      @Valid @RequestBody VaccinationRequest request) {
        return vaccinationService.update(userDetails.getUsername(), id, request);
    }
}
