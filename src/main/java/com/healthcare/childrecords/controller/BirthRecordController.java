package com.healthcare.childrecords.controller;

import com.healthcare.childrecords.dto.request.BirthRecordRequest;
import com.healthcare.childrecords.dto.response.BirthRecordResponse;
import com.healthcare.childrecords.service.BirthRecordService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/birth-records")
@RequiredArgsConstructor
@Tag(name = "Birth Records", description = "Child birth record management endpoint")
public class BirthRecordController {

    private final BirthRecordService birthRecordService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('DOCTOR')")
    public BirthRecordResponse create(@AuthenticationPrincipal UserDetails userDetails,
                                      @Valid @RequestBody BirthRecordRequest request) {
        return birthRecordService.create(userDetails.getUsername(), request);
    }

    @GetMapping("/child/{childId}")
    public ResponseEntity<BirthRecordResponse> getByChildId(@AuthenticationPrincipal UserDetails userDetails,
                                                            @PathVariable Long childId) {
        return birthRecordService.getByChildId(userDetails.getUsername(), childId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PutMapping("/child/{childId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public BirthRecordResponse update(@AuthenticationPrincipal UserDetails userDetails,
                                      @PathVariable Long childId,
                                      @Valid @RequestBody BirthRecordRequest request) {
        return birthRecordService.update(userDetails.getUsername(), childId, request);
    }
}
