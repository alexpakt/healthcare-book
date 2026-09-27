package com.healthcare.childrecords.controller;

import com.healthcare.childrecords.dto.request.VaccinationScheduleRequest;
import com.healthcare.childrecords.dto.response.VaccinationScheduleResponse;
import com.healthcare.childrecords.service.VaccinationScheduleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vaccination-schedules")
@RequiredArgsConstructor
@Tag(name = "Vaccination Schedules", description = "Child vaccination schedule management endpoint")
public class VaccinationScheduleController {

    private final VaccinationScheduleService vaccinationScheduleService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PARENT')")
    public ResponseEntity<List<VaccinationScheduleResponse>> getAllSchedules() {
        return ResponseEntity.ok(vaccinationScheduleService.getAllSchedules());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PARENT')")
    public ResponseEntity<VaccinationScheduleResponse> getScheduleById(@PathVariable Long id) {
        return ResponseEntity.ok(vaccinationScheduleService.getScheduleById(id));
    }

    @GetMapping("/vaccine/{vaccineName}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PARENT')")
    public ResponseEntity<List<VaccinationScheduleResponse>> getScheduleByVaccineName(
            @PathVariable String vaccineName) {
        return ResponseEntity.ok(vaccinationScheduleService.getScheduleByVaccineName(vaccineName));
    }

    @GetMapping("/age/{ageMonths}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PARENT')")
    public ResponseEntity<List<VaccinationScheduleResponse>> getSchedulesByAge(
            @PathVariable Integer ageMonths) {
        return ResponseEntity.ok(vaccinationScheduleService.getSchedulesByAge(ageMonths));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VaccinationScheduleResponse> createSchedule(
            @Valid @RequestBody VaccinationScheduleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(vaccinationScheduleService.createSchedule(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VaccinationScheduleResponse> updateSchedule(
            @PathVariable Long id,
            @Valid @RequestBody VaccinationScheduleRequest request) {
        return ResponseEntity.ok(vaccinationScheduleService.updateSchedule(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        vaccinationScheduleService.deleteSchedule(id);
        return ResponseEntity.noContent().build();
    }
}
