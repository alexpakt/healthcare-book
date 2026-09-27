package com.healthcare.childrecords.controller;

import com.healthcare.childrecords.dto.response.VaccineResponse;
import com.healthcare.childrecords.service.VaccineService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/vaccines")
@RequiredArgsConstructor
@Tag(name = "Vaccines", description = "Vaccine management endpoint")
public class VaccineController {

    private final VaccineService vaccineService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PARENT')")
    public ResponseEntity<List<VaccineResponse>> getAllVaccines() {
        return ResponseEntity.ok(vaccineService.getAllVaccines());
    }
}
