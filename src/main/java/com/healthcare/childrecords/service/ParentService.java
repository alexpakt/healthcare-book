package com.healthcare.childrecords.service;

import com.healthcare.childrecords.dto.response.ParentResponse;
import com.healthcare.childrecords.entity.Parent;
import com.healthcare.childrecords.exception.AccessDeniedException;
import com.healthcare.childrecords.exception.ResourceNotFoundException;
import com.healthcare.childrecords.repository.ParentRepository;
import com.healthcare.childrecords.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ParentService {

    private final ParentRepository parentRepository;
    private final UserRepository userRepository;

    public ParentResponse getMyProfile(String username) {
        return toResponse(findByUsername(username));
    }

    public ParentResponse updateMyProfile(String username, String phoneNumber, String address) {
        Parent parent = findByUsername(username);
        if (phoneNumber != null) parent.setPhoneNumber(phoneNumber);
        if (address != null) parent.setAddress(address);
        return toResponse(parentRepository.save(parent));
    }

    public Parent findByUsername(String username) {
        Long userId = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username))
                .getId();
        return parentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent profile not found for user: " + username));
    }

    public Parent findById(Long id) {
        return parentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found: " + id));
    }

    public void verifyParentOwnsChild(Parent parent, Long childId) {
        boolean owns = parent.getChildren().stream().anyMatch(c -> c.getId().equals(childId));
        if (!owns) {
            throw new AccessDeniedException("Parent does not have access to this child");
        }
    }

    private ParentResponse toResponse(Parent p) {
        return new ParentResponse(p.getId(), p.getFirstName(), p.getLastName(),
                p.getEmail(), p.getPhoneNumber(), p.getAddress());
    }
}
