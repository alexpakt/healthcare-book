package com.healthcare.childrecords.controller;

import com.healthcare.childrecords.dto.request.UpdateParentProfileRequest;
import com.healthcare.childrecords.dto.response.ParentResponse;
import com.healthcare.childrecords.service.ParentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parents")
@RequiredArgsConstructor
@Tag(name = "Parents", description = "Parent management endpoint")
public class ParentController {

    private final ParentService parentService;

    @GetMapping("/me")
    public ParentResponse getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return parentService.getMyProfile(userDetails.getUsername());
    }

    @PatchMapping("/me")
    public ParentResponse updateMyProfile(@AuthenticationPrincipal UserDetails userDetails,
                                          @Valid @RequestBody UpdateParentProfileRequest request) {
        return parentService.updateMyProfile(userDetails.getUsername(), request);
    }
}
