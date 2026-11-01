package com.healthcare.childrecords.service;

import com.healthcare.childrecords.dto.request.UpdateParentProfileRequest;
import com.healthcare.childrecords.dto.response.ParentResponse;
import com.healthcare.childrecords.entity.Parent;
import com.healthcare.childrecords.exception.ResourceNotFoundException;
import com.healthcare.childrecords.repository.ParentRepository;
import com.healthcare.childrecords.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParentService {

    private final ParentRepository parentRepository;
    private final UserRepository userRepository;

    public ParentResponse getMyProfile(String username) {
        return toResponse(findByUsername(username));
    }

    @Transactional
    public ParentResponse updateMyProfile(String username, UpdateParentProfileRequest request) {
        Parent parent = findByUsername(username);
        if (request.firstName() != null) parent.setFirstName(request.firstName());
        if (request.lastName() != null) parent.setLastName(request.lastName());
        if (request.email() != null) parent.setEmail(request.email());
        if (request.phoneNumber() != null) parent.setPhoneNumber(request.phoneNumber());
        if (request.address() != null) parent.setAddress(request.address());
        return toResponse(parentRepository.save(parent));
    }

    public Parent findByUsername(String username) {
        Long userId = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Ο χρήστης δεν βρέθηκε: " + username))
                .getId();
        return parentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Δεν βρέθηκε προφίλ γονέα για τον χρήστη: " + username));
    }

    public Parent findById(Long id) {
        return parentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ο γονέας δεν βρέθηκε: " + id));
    }

    private ParentResponse toResponse(Parent p) {
        return new ParentResponse(p.getId(), p.getFirstName(), p.getLastName(),
                p.getEmail(), p.getPhoneNumber(), p.getAddress());
    }
}
