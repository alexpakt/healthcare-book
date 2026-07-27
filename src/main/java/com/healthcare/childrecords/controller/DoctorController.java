package com.healthcare.childrecords.controller;

import com.healthcare.childrecords.dto.response.DoctorResponse;
import com.healthcare.childrecords.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping
    public List<DoctorResponse> getAll() {
        return doctorService.getAll();
    }

    @GetMapping("/{id}")
    public DoctorResponse getById(@PathVariable Long id) {
        return doctorService.getById(id);
    }

    @GetMapping("/me")
    public DoctorResponse getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return doctorService.getMyProfile(userDetails.getUsername());
    }

    @PatchMapping("/me")
    public DoctorResponse updateMyProfile(@AuthenticationPrincipal UserDetails userDetails,
                                          @RequestBody Map<String, String> updates) {
        return doctorService.updateMyProfile(
                userDetails.getUsername(),
                updates.get("phoneNumber"),
                updates.get("specialization"));
    }
}
