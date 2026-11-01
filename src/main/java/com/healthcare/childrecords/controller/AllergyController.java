package com.healthcare.childrecords.controller;

import com.healthcare.childrecords.dto.request.AllergyRequest;
import com.healthcare.childrecords.dto.response.AllergyResponse;
import com.healthcare.childrecords.service.AllergyService;
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
@RequestMapping("/api/allergies")
@RequiredArgsConstructor
@Tag(name = "Allergies", description = "Child allergies endpoints")
public class AllergyController {

    private final AllergyService allergyService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('DOCTOR')")
    public AllergyResponse create(@AuthenticationPrincipal UserDetails userDetails,
                                  @Valid @RequestBody AllergyRequest request) {
        return allergyService.create(userDetails.getUsername(), request);
    }

    @GetMapping("/child/{childId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PARENT')")
    public List<AllergyResponse> getForChild(@AuthenticationPrincipal UserDetails userDetails,
                                             @PathVariable Long childId) {
        return allergyService.getForChild(userDetails.getUsername(), childId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PARENT')")
    public AllergyResponse getById(@AuthenticationPrincipal UserDetails userDetails,
                                   @PathVariable Long id) {
        return allergyService.getById(userDetails.getUsername(), id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public AllergyResponse update(@AuthenticationPrincipal UserDetails userDetails,
                                  @PathVariable Long id,
                                  @Valid @RequestBody AllergyRequest request) {
        return allergyService.update(userDetails.getUsername(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('DOCTOR')")
    public void delete(@AuthenticationPrincipal UserDetails userDetails,
                       @PathVariable Long id) {
        allergyService.delete(userDetails.getUsername(), id);
    }
}
