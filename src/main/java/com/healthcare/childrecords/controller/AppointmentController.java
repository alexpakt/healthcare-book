package com.healthcare.childrecords.controller;

import com.healthcare.childrecords.dto.request.AppointmentRequest;
import com.healthcare.childrecords.dto.response.AppointmentResponse;
import com.healthcare.childrecords.entity.AppointmentStatus;
import com.healthcare.childrecords.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentResponse create(@AuthenticationPrincipal UserDetails userDetails,
                                      @Valid @RequestBody AppointmentRequest request) {
        return appointmentService.create(userDetails.getUsername(), request);
    }

    @GetMapping
    public List<AppointmentResponse> getMyAppointments(@AuthenticationPrincipal UserDetails userDetails) {
        return appointmentService.getMyAppointments(userDetails.getUsername());
    }

    @GetMapping("/child/{childId}")
    public List<AppointmentResponse> getForChild(@AuthenticationPrincipal UserDetails userDetails,
                                                 @PathVariable Long childId) {
        return appointmentService.getForChild(userDetails.getUsername(), childId);
    }

    @PatchMapping("/{id}/status")
    public AppointmentResponse updateStatus(@AuthenticationPrincipal UserDetails userDetails,
                                            @PathVariable Long id,
                                            @RequestBody Map<String, String> body) {
        AppointmentStatus status = AppointmentStatus.valueOf(body.get("status"));
        return appointmentService.updateStatus(userDetails.getUsername(), id, status);
    }
}
