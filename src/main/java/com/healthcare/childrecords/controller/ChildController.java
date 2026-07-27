package com.healthcare.childrecords.controller;

import com.healthcare.childrecords.dto.request.ChildRequest;
import com.healthcare.childrecords.dto.response.ChildResponse;
import com.healthcare.childrecords.service.ChildService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/children")
@RequiredArgsConstructor
public class ChildController {

    private final ChildService childService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChildResponse create(@AuthenticationPrincipal UserDetails userDetails,
                                @Valid @RequestBody ChildRequest request) {
        return childService.create(userDetails.getUsername(), request);
    }

    @GetMapping
    public List<ChildResponse> getMyChildren(@AuthenticationPrincipal UserDetails userDetails) {
        return childService.getMyChildren(userDetails.getUsername());
    }

    @GetMapping("/{id}")
    public ChildResponse getById(@AuthenticationPrincipal UserDetails userDetails,
                                 @PathVariable Long id) {
        return childService.getById(userDetails.getUsername(), id);
    }

    @PutMapping("/{id}")
    public ChildResponse update(@AuthenticationPrincipal UserDetails userDetails,
                                @PathVariable Long id,
                                @Valid @RequestBody ChildRequest request) {
        return childService.update(userDetails.getUsername(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal UserDetails userDetails,
                       @PathVariable Long id) {
        childService.delete(userDetails.getUsername(), id);
    }

    @PostMapping("/{childId}/doctors/{doctorId}")
    public ChildResponse assignDoctor(@AuthenticationPrincipal UserDetails userDetails,
                                      @PathVariable Long childId,
                                      @PathVariable Long doctorId) {
        return childService.assignDoctor(userDetails.getUsername(), childId, doctorId);
    }

    @DeleteMapping("/{childId}/doctors/{doctorId}")
    public ChildResponse removeDoctor(@AuthenticationPrincipal UserDetails userDetails,
                                      @PathVariable Long childId,
                                      @PathVariable Long doctorId) {
        return childService.removeDoctor(userDetails.getUsername(), childId, doctorId);
    }
}
