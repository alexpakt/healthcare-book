package com.healthcare.childrecords.controller;

import com.healthcare.childrecords.dto.request.MedicalRecordRequest;
import com.healthcare.childrecords.dto.response.MedicalRecordResponse;
import com.healthcare.childrecords.service.MedicalRecordService;
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
@RequestMapping("/api/medical-records")
@RequiredArgsConstructor
@Tag(name = "Medical Records", description = "Child medical record management endpoint")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('DOCTOR')")
    public MedicalRecordResponse create(@AuthenticationPrincipal UserDetails userDetails,
                                        @Valid @RequestBody MedicalRecordRequest request) {
        return medicalRecordService.create(userDetails.getUsername(), request);
    }

    @GetMapping("/child/{childId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PARENT')")
    public List<MedicalRecordResponse> getForChild(@AuthenticationPrincipal UserDetails userDetails,
                                                   @PathVariable Long childId) {
        return medicalRecordService.getForChild(userDetails.getUsername(), childId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PARENT')")
    public MedicalRecordResponse getById(@AuthenticationPrincipal UserDetails userDetails,
                                         @PathVariable Long id) {
        return medicalRecordService.getById(userDetails.getUsername(), id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public MedicalRecordResponse update(@AuthenticationPrincipal UserDetails userDetails,
                                        @PathVariable Long id,
                                        @Valid @RequestBody MedicalRecordRequest request) {
        return medicalRecordService.update(userDetails.getUsername(), id, request);
    }
}
